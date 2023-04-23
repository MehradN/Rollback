package ir.mehradn.rollback.util.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.mixin.GameRendererAccessor;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import ir.mehradn.rollback.util.gson.MetadataUpdaterVersionAdapter;
import ir.mehradn.rollback.util.gson.PathAdapter;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Environment(EnvType.CLIENT)
public class BackupManager {
    public transient final Path rollbackDirectory;
    public transient final Path iconsDirectory;
    @SerializedName("version") public MetadataUpdater.Version version = MetadataUpdater.Version.LATEST_VERSION;
    @SerializedName("worlds") public Map<String, RollbackWorld> worlds = new HashMap<>();
    private static final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, PathAdapter.INSTANCE)
        .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.INSTANCE)
        .registerTypeAdapter(MetadataUpdater.Version.class, MetadataUpdaterVersionAdapter.INSTANCE)
        .create();

    private BackupManager() {
        this.rollbackDirectory = Minecraft.getInstance().getLevelSource().getBackupPath().resolve("rollbacks");
        this.iconsDirectory = this.rollbackDirectory.resolve("icons");
    }

    public RollbackWorld getWorld(String worldName) {
        if (!this.worlds.containsKey(worldName))
            this.worlds.put(worldName, new RollbackWorld());
        return this.worlds.get(worldName);
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

    public boolean createRollbackBackup(MinecraftServer server, String name) {
        Rollback.LOGGER.info("Creating a rollback backup...");
        LevelStorageSource.LevelStorageAccess levelAccess = ((MinecraftServerExpanded)server).getLevelAccess();
        String worldName = levelAccess.getLevelId();
        RollbackWorld world = getWorld(worldName);

        while (world.backups.size() >= RollbackConfig.maxBackupsPerWorld())
            deleteBackup(worldName, 0);
        deleteGhostIcons(worldName);

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
                worldName + "_" + (world.lastID + 1),
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
            path3 = this.iconsDirectory.resolve(FileUtil.findAvailableName(this.iconsDirectory,
                worldName + "_" + (world.lastID + 1), ".png"));
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
        RollbackBackup backup = new RollbackBackup(path2, path3, LocalDateTime.now(), daysPlayed, name);
        world.backups.add(backup);
        world.lastID++;

        saveMetadata();
        return true;
    }

    public boolean deleteBackup(String worldName, int index) {
        Rollback.LOGGER.info("Deleting the backup #{}...", index);
        RollbackWorld world = getWorld(worldName);

        if (world.backups.size() <= index || index < -world.backups.size())
            return true;
        if (index < 0)
            index += world.backups.size();
        RollbackBackup backup = world.backups.get(index);

        Rollback.LOGGER.debug("Deleting the files...");
        try {
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.backupPath));
            if (backup.iconPath != null)
                Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
        } catch (IOException e) {
            showError("rollback.deleteBackup.failed", "Failed to delete the files!", e);
            return false;
        }

        world.backups.remove(index);
        saveMetadata();
        return true;
    }

    public boolean rollbackTo(String worldName, RollbackBackup backup) {
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());
        Minecraft client = Minecraft.getInstance();

        Rollback.LOGGER.debug("Deleting the current save...");
        try (LevelStorageSource.LevelStorageAccess levelAccess = client.getLevelSource().createAccess(worldName)) {
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
        RollbackWorld world = getWorld(worldName);
        while (world.backups.size() > 0)
            deleteBackup(worldName, 0);
        this.worlds.remove(worldName);
        saveMetadata();
    }

    public void saveMetadata() {
        Rollback.LOGGER.info("Saving metadata file...");
        Path metadataFilePath = Minecraft.getInstance().getLevelSource().getBackupPath().resolve("rollbacks/rollbacks.json");
        try {
            Files.createDirectories(this.rollbackDirectory);
            Files.createDirectories(this.iconsDirectory);
            try (FileWriter writer = new FileWriter(metadataFilePath.toFile())) {
                gson.toJson(this, writer);
            }
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

    private void deleteGhostIcons(String worldName) {
        RollbackWorld world = getWorld(worldName);
        for (RollbackBackup backup : world.backups)
            if (backup.iconPath != null && !Files.isRegularFile(this.rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
    }

    public static BackupManager loadMetadata() {
        Rollback.LOGGER.info("Loading metadata file...");
        Path metadataFilePath = Minecraft.getInstance().getLevelSource().getBackupPath().resolve("rollbacks/rollbacks.json");
        BackupManager backupManager;
        boolean save = false;

        try (FileReader reader = new FileReader(metadataFilePath.toFile())) {
            JsonObject metadata = JsonParser.parseReader(reader).getAsJsonObject();
            MetadataUpdater updater = new MetadataUpdater(metadata);
            if (updater.getVersion().isOutdated()) {
                metadata = updater.update();
                save = true;
            }
            backupManager = gson.fromJson(metadata, BackupManager.class);
        } catch (FileNotFoundException e) {
            Rollback.LOGGER.warn("Metadata file not found! Creating a new one...");
            backupManager = new BackupManager();
            save = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (save)
            backupManager.saveMetadata();
        return backupManager;
    }
}
