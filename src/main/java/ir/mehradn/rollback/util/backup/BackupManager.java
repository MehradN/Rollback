package ir.mehradn.rollback.util.backup;

import com.google.gson.*;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.mixin.GameRendererAccessor;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Environment(EnvType.CLIENT)
public class BackupManager {
    public final Path rollbackDirectory;
    public final Path iconsDirectory;
    private final Path metadataFilePath;
    private JsonObject metadata;

    public BackupManager() {
        this.rollbackDirectory = Minecraft.getInstance().getLevelSource().getBackupPath().resolve("rollbacks");
        this.iconsDirectory = this.rollbackDirectory.resolve("icons");
        this.metadataFilePath = this.rollbackDirectory.resolve("rollbacks.json");
        try {
            loadMetadata();
        } catch (FileNotFoundException e) {
            Rollback.LOGGER.warn("Metadata file not found! Creating a new one...");
            this.metadata = new JsonObject();
            saveMetadata();
        }
    }

    public boolean getAutomated(String worldName) {
        JsonObject worldObject = getWorldObject(worldName);
        return worldObject.get("automated").getAsBoolean();
    }

    public void setAutomated(String worldName, boolean enabled) {
        getWorldObject(worldName).addProperty("automated", enabled);
        saveMetadata();
    }

    public boolean getPrompted(String worldName) {
        JsonObject worldObject = getWorldObject(worldName);
        return (worldObject.get("automated").getAsBoolean() || worldObject.get("prompted").getAsBoolean());
    }

    public void setPromptAnswer(String worldName, boolean automated) {
        JsonObject worldObject = getWorldObject(worldName);
        worldObject.addProperty("automated", automated);
        worldObject.addProperty("prompted", true);
        saveMetadata();
    }

    public Triple<Integer, Integer, Integer> getTimerInformation(String worldName) {
        JsonObject worldObject = getWorldObject(worldName);
        int daysPassed = worldObject.get("days_passed").getAsInt();
        int sinceDay = worldObject.get("since_day").getAsInt();
        int sinceBackup = worldObject.get("since_backup").getAsInt();
        return Triple.of(daysPassed, sinceDay, sinceBackup);
    }

    public void setTimerInformation(String worldName, int daysPassed, int sinceBackup) {
        JsonObject worldObject = getWorldObject(worldName);
        worldObject.addProperty("days_passed", daysPassed);
        worldObject.addProperty("since_backup", sinceBackup);
        saveMetadata();
    }

    public void setTimerInformation(String worldName, int daysPassed, int sinceDay, int sinceBackup) {
        JsonObject worldObject = getWorldObject(worldName);
        worldObject.addProperty("days_passed", daysPassed);
        worldObject.addProperty("since_day", sinceDay);
        worldObject.addProperty("since_backup", sinceBackup);
        saveMetadata();
    }

    public List<RollbackBackup> getRollbacksFor(String worldName) {
        ArrayList<RollbackBackup> list = new ArrayList<>();
        JsonArray array = getWorldObject(worldName).getAsJsonArray("backups");
        for (JsonElement elm : array)
            list.add(new RollbackBackup(worldName, elm.getAsJsonObject()));
        return list;
    }

    public boolean createNormalBackup(LevelSummary summary) {
        Rollback.LOGGER.info("Creating a manual backup...");
        Minecraft minecraft = Minecraft.getInstance();
        try (LevelStorageSource.LevelStorageAccess levelAccess = minecraft.getLevelSource().createAccess(summary.getLevelId())) {
            long size = levelAccess.makeWorldBackup();
            minecraft.getToasts().addToast(
                new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                    Component.translatable("selectWorld.edit.backupCreated", levelAccess.getLevelId()),
                    Component.translatable("selectWorld.edit.backupSize", Mth.ceil(size / 1048576.0))
                ));
            return true;
        } catch (IOException e) {
            showError("selectWorld.edit.backupFailed", "Failed to create a backup of world!", e);
            return false;
        }
    }

    public boolean createRollbackBackup(MinecraftServer server, boolean automated) {
        Rollback.LOGGER.info("Creating a rollback backup...");
        LevelStorageSource.LevelStorageAccess levelAccess = ((MinecraftServerExpanded)server).getLevelAccess();
        String worldName = levelAccess.getLevelId();
        LocalDateTime now = LocalDateTime.now();
        JsonObject worldObject = getWorldObject(worldName);
        JsonArray array = worldObject.getAsJsonArray("backups");

        while (array.size() >= RollbackConfig.maxBackupsPerWorld())
            deleteBackup(worldName, 0);
        deleteNonexistentIcons(worldName);

        Rollback.LOGGER.debug("Saving the world...");
        boolean f = server.saveEverything(true, true, true);
        if (!f) {
            showError("rollback.createBackup.failed", "Failed to create a backup of world!", null);
            return false;
        }

        Rollback.LOGGER.debug("Creating a backup...");
        try {
            levelAccess.makeWorldBackup();
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to create a backup of world!", e);
            return false;
        }

        Rollback.LOGGER.debug("Moving the backup...");
        Path path1 = ((LevelStorageAccessExpanded)levelAccess).getLatestBackupPath();
        Path path2;
        try {
            path2 = this.rollbackDirectory.resolve(FileUtil.findAvailableName(
                this.rollbackDirectory,
                now.format(RollbackBackup.TIME_FORMATTER) + "_" + worldName,
                ".zip"
            ));
            Files.move(path1, path2);
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to move the backup file!", e);
            return false;
        }

        Rollback.LOGGER.debug("Creating an icon...");
        Path path3;
        try {
            path3 = this.iconsDirectory.resolve(FileUtil.findAvailableName(this.iconsDirectory, levelAccess.getLevelId(), ".png"));
            Path finalPath = path3;
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> {
                GameRenderer renderer = minecraft.gameRenderer;
                ((GameRendererAccessor)renderer).InvokeTakeAutoScreenshot(finalPath);
            });
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to make an icon for the backup!", e);
            return false;
        }

        ClientLevel level = Minecraft.getInstance().level;
        int daysPlayed = (level == null ? -1 : (int)level.getDayTime() / 24000);

        Rollback.LOGGER.debug("Adding the metadata...");
        path2 = this.rollbackDirectory.relativize(path2);
        path3 = this.rollbackDirectory.relativize(path3);
        RollbackBackup backup = new RollbackBackup(worldName, path2, path3, LocalDateTime.now(), daysPlayed);
        array.add(backup.toObject());
        if (automated) {
            worldObject.addProperty("days_passed", 0);
            worldObject.addProperty("since_day", 0);
            worldObject.addProperty("since_backup", 0);
        }
        saveMetadata();

        return true;
    }

    public boolean deleteBackup(String worldName, int index) {
        Rollback.LOGGER.info("Deleting the backup #{}...", index);
        JsonArray array = getWorldObject(worldName).getAsJsonArray("backups");

        if (array.size() <= index || index < -array.size())
            return true;
        if (index < 0)
            index += array.size();

        RollbackBackup backup = new RollbackBackup(worldName, array.get(index).getAsJsonObject());

        Rollback.LOGGER.debug("Deleting the files...");
        try {
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.backupPath));
            if (backup.iconPath != null)
                Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
        } catch (IOException e) {
            showError("rollback.deleteBackup.failed", "Failed to delete the files!", e);
            return false;
        }

        array.remove(index);
        saveMetadata();

        return true;
    }

    public boolean rollbackTo(RollbackBackup backup) {
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());
        Minecraft client = Minecraft.getInstance();

        Rollback.LOGGER.debug("Deleting the current save...");
        try (LevelStorageSource.LevelStorageAccess levelAccess = client.getLevelSource().createAccess(backup.worldName)) {
            levelAccess.deleteLevel();
        } catch (IOException e) {
            showError("rollback.rollback.failed", "Failed to delete the current save!", e);
            return false;
        }

        Rollback.LOGGER.debug("Extracting the backup to save directory...");
        Path source = this.rollbackDirectory.resolve(backup.backupPath.toString());
        Path dest = client.getLevelSource().getBaseDir();
        try (ZipFile zip = new ZipFile(source.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = dest.resolve(entry.getName());
                if (entry.isDirectory())
                    Files.createDirectories(path);
                else {
                    Files.createDirectories(path.getParent());
                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(path)) {
                        in.transferTo(out);
                    }
                }
            }
        } catch (IOException e) {
            showError("rollback.rollback.failed", "Failed to extract the backup to save directory!", e);
            return false;
        }

        return true;
    }

    public void deleteAllBackupsFor(String worldName) {
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", worldName);

        JsonArray array = getWorldObject(worldName).getAsJsonArray("backups");
        while (array.size() > 0)
            deleteBackup(worldName, 0);
        this.metadata.getAsJsonObject("worlds").remove(worldName);

        saveMetadata();
    }

    private JsonObject getWorldObject(String worldName) {
        if (!this.metadata.has("worlds"))
            this.metadata.add("worlds", new JsonObject());
        JsonObject worldsData = this.metadata.getAsJsonObject("worlds");

        if (!worldsData.has(worldName))
            worldsData.add(worldName, new JsonObject());
        JsonObject worldObject = worldsData.getAsJsonObject(worldName);

        if (!worldObject.has("automated"))
            worldObject.addProperty("automated", false);
        if (!worldObject.has("prompted"))
            worldObject.addProperty("prompted", false);
        if (!worldObject.has("days_passed"))
            worldObject.addProperty("days_passed", 0);
        if (!worldObject.has("since_day"))
            worldObject.addProperty("since_day", 0);
        if (!worldObject.has("since_backup"))
            worldObject.addProperty("since_backup", 0);
        if (!worldObject.has("backups"))
            worldObject.add("backups", new JsonArray());

        return worldObject;
    }

    private void loadMetadata() throws FileNotFoundException {
        this.metadata = JsonParser.parseReader(new FileReader(this.metadataFilePath.toFile())).getAsJsonObject();
        if (MetadataUpdater.getVersion(this.metadata).isLessThan(0, 3)) {
            MetadataUpdater updater = new MetadataUpdater(this.metadata);
            this.metadata = updater.update();
            saveMetadata();
        }
    }

    private void saveMetadata() {
        Rollback.LOGGER.info("Saving metadata file...");
        try {
            this.metadata.addProperty("version", "0.3");
            Files.createDirectories(this.rollbackDirectory);
            Files.createDirectories(this.iconsDirectory);
            FileWriter writer = new FileWriter(this.metadataFilePath.toFile());
            Gson gson = new GsonBuilder()/*.setPrettyPrinting()*/.create();
            gson.toJson(this.metadata, writer);
            writer.close();
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the metadata file!", e);
            throw new RuntimeException(e);
        }
    }

    private void showError(String title, String info, Throwable exception) {
        Rollback.LOGGER.error(info, exception);
        Minecraft.getInstance().getToasts().addToast(new SystemToast(
            SystemToast.SystemToastIds.WORLD_BACKUP,
            Component.translatable(title),
            Component.literal(info)
        ));
    }

    private void deleteNonexistentIcons(String worldName) {
        JsonArray array = getWorldObject(worldName).getAsJsonArray("backups");
        for (JsonElement elm : array) {
            JsonObject obj = elm.getAsJsonObject();
            if (!obj.has("icon_file"))
                continue;
            String iconPath = obj.get("icon_file").getAsString();
            if (!Files.isRegularFile(this.rollbackDirectory.resolve(iconPath)))
                obj.remove("icon_file");
        }
    }
}
