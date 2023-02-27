package ir.mehradn.rollback.util.backup;

import com.google.gson.*;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.mixin.GameRendererAccessor;
import ir.mehradn.rollback.util.mixin.LevelStorageSessionExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

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
    private JsonObject metadata;
    public final Path rollbackDirectory;
    public final Path iconsDirectory;
    private final Path metadataFilePath;

    public BackupManager() {
        this.rollbackDirectory = MinecraftClient.getInstance().getLevelStorage().getBackupsDirectory().resolve("rollbacks");
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

    private void loadMetadata() throws FileNotFoundException {
        this.metadata = JsonParser.parseReader(new FileReader(this.metadataFilePath.toFile())).getAsJsonObject();
    }

    private void saveMetadata() {
        Rollback.LOGGER.info("Saving metadata file...");
        try {
            Files.createDirectories(this.rollbackDirectory);
            Files.createDirectories(this.iconsDirectory);
            FileWriter writer = new FileWriter(this.metadataFilePath.toFile());
            Gson gson = new GsonBuilder()/*.setPrettyPrinting()*/.create();
            gson.toJson(this.metadata, writer);
            writer.close();
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the metadata file!");
            throw new RuntimeException(e);
        }
    }

    private void showError(String title, String info) {
        Rollback.LOGGER.error(info);
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(
            SystemToast.Type.WORLD_BACKUP,
            Text.translatable(title),
            Text.literal(info)
        ));
    }

    public boolean createNormalBackup(LevelSummary summary) {
        Rollback.LOGGER.info("Creating a manual backup...");
        try (LevelStorage.Session session = MinecraftClient.getInstance().getLevelStorage().createSession(summary.getName())) {
            long size = session.createBackup();
            MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("selectWorld.edit.backupCreated", session.getDirectoryName()),
                    Text.translatable("selectWorld.edit.backupSize", MathHelper.ceil(size / 1048576.0))
                ));
            return true;
        } catch (IOException e) {
            showError("selectWorld.edit.backupFailed", "Failed to create a backup of world!");
            return false;
        }
    }

    public boolean createRollbackBackup(MinecraftServer server) {
        Rollback.LOGGER.info("Creating a rollback backup...");
        LevelStorage.Session session = ((MinecraftServerExpanded)server).getSession();
        String worldName = session.getLevelSummary().getName();
        LocalDateTime now = LocalDateTime.now();
        if (!this.metadata.has(worldName))
            this.metadata.add(worldName, new JsonArray());
        JsonArray array = this.metadata.getAsJsonArray(worldName);

        while (array.size() >= RollbackConfig.getMaxBackupsPerWorld())
            deleteBackup(worldName, 0);

        Rollback.LOGGER.debug("Saving the world...");
        boolean f = server.saveAll(true, true, true);
        if (!f) {
            showError("rollback.createBackup.failed", "Failed to create a backup of world!");
            return false;
        }

        Rollback.LOGGER.debug("Creating a backup...");
        try {
            session.createBackup();
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to create a backup of world!");
            return false;
        }

        Rollback.LOGGER.debug("Moving the backup...");
        Path path1 = ((LevelStorageSessionExpanded)session).getLatestBackupPath();
        Path path2;
        try {
            path2 = this.rollbackDirectory.resolve(PathUtil.getNextUniqueName(
                this.rollbackDirectory,
                now.format(RollbackBackup.TIME_FORMATTER) + "_" + worldName,
                ".zip"
            ));
            Files.move(path1, path2);
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to move the backup file!");
            return false;
        }

        Rollback.LOGGER.debug("Creating an icon...");
        Path path3;
        try {
            path3 = this.iconsDirectory.resolve(PathUtil.getNextUniqueName(this.iconsDirectory, session.getDirectoryName(), ".png"));
            Path finalPath = path3;
            MinecraftClient.getInstance().execute(() -> {
                GameRenderer renderer = MinecraftClient.getInstance().gameRenderer;
                ((GameRendererAccessor)renderer).InvokeUpdateWorldIcon(finalPath);
            });
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to make an icon for the backup!");
            return false;
        }

        int daysPlayed = (int)(MinecraftClient.getInstance().world.getTimeOfDay() / 24000);

        Rollback.LOGGER.debug("Adding the metadata...");
        path2 = this.rollbackDirectory.relativize(path2);
        path3 = this.rollbackDirectory.relativize(path3);
        RollbackBackup backup = new RollbackBackup(worldName, path2, path3, LocalDateTime.now(), daysPlayed);
        array.add(backup.toObject());
        saveMetadata();

        return true;
    }

    public boolean deleteBackup(String worldName, int index) {
        if (!this.metadata.has(worldName))
            return true;

        Rollback.LOGGER.info("Deleting the backup #{}...", index);
        JsonArray array = this.metadata.getAsJsonArray(worldName);
        if (index < 0)
            index += array.size();
        if (index >= array.size())
            return true;

        RollbackBackup backup = new RollbackBackup(worldName, array.get(index).getAsJsonObject());

        Rollback.LOGGER.debug("Deleting the files...");
        try {
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.backupPath));
        } catch (IOException e) {
            showError("rollback.deleteBackup.failed", "Failed to delete the files!");
            return false;
        }

        array.remove(index);
        saveMetadata();

        return true;
    }

    public List<RollbackBackup> getRollbacksFor(String worldName) {
        ArrayList<RollbackBackup> list = new ArrayList<>();
        if (!this.metadata.has(worldName))
            return list;

        JsonArray array = this.metadata.getAsJsonArray(worldName);
        for (JsonElement elm : array)
            list.add(new RollbackBackup(worldName, elm.getAsJsonObject()));
        return list;
    }

    public boolean rollbackTo(RollbackBackup backup) {
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());
        MinecraftClient client = MinecraftClient.getInstance();

        Rollback.LOGGER.debug("Deleting the current save...");
        try (LevelStorage.Session session = client.getLevelStorage().createSession(backup.worldName)) {
            session.deleteSessionLock();
        } catch (IOException e) {
            showError("rollback.rollback.failed", "Failed to delete the current save!");
            return false;
        }

        Rollback.LOGGER.debug("Extracting the backup to save directory...");
        Path source = this.rollbackDirectory.resolve(backup.backupPath.toString());
        Path dest = client.getLevelStorage().getSavesDirectory();
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
            showError("rollback.rollback.failed", "Failed to extract the backup to save directory!");
            return false;
        }

        return true;
    }

    public void deleteAllBackupsFor(String worldName) {
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", worldName);
        if (!this.metadata.has(worldName))
            return;

        while (this.metadata.getAsJsonArray(worldName).size() > 0)
            deleteBackup(worldName, 0);
        this.metadata.remove(worldName);

        this.saveMetadata();
    }

    /*
    public void cleanUp() {
         List<String> worldNames;
         try {
             LevelStorage storage = MinecraftClient.getInstance().getLevelStorage();
             LevelStorage.LevelList levelList = storage.getLevelList();
             List<LevelSummary> summaries = storage.loadSummaries(levelList).get();
             worldNames = summaries.stream().map(LevelSummary::getName).toList();
         } catch (Exception e) {
             return;
         }

         for (String world : this.backupInfo.keySet())
             if (!worldNames.contains(world))
                 this.backupInfo.remove(world);

         List<Path> providedBackups;
         List<Path> providedIcons;
         try (Stream<Path> stream1 = Files.list(this.rollbackDirectory);
              Stream<Path> stream2 = Files.list(this.iconsDirectory)) {
             providedBackups = stream1.filter((path) -> !(Files.isDirectory(path)) || path.getFileName().toString().equals("rollbacks.json")).toList();
             providedIcons = stream2.toList();
         } catch (IOException e) {
             return;
         }

         ArrayList<String> expectedBackups = new ArrayList<>();
         ArrayList<String> expectedIcons = new ArrayList<>();
         for (String world : this.backupInfo.keySet()) {
             JsonArray backups = this.backupInfo.getAsJsonArray(world);
             for (JsonElement backup : backups) {
                 String file = backup.getAsJsonObject().get("backup_file").getAsString();
                 if (providedBackups.contains(Path.of(file))) {
                     expectedBackups.add(file);
                     expectedIcons.add(backup.getAsJsonObject().get("icon_file").getAsString());
                 } else
                     backups.remove(backup);
             }
         }

         for (Path path : providedBackups) {
             if (!expectedBackups.contains(path.getFileName().toString())) {
                 try {
                     Files.deleteIfExists(this.rollbackDirectory.resolve(path));
                 } catch (IOException ignored) {}
             }
         }
         for (Path path : providedIcons) {
             if (!expectedIcons.contains(path.getFileName().toString())) {
                 try {
                     Files.deleteIfExists(this.iconsDirectory.resolve(path));
                 } catch (IOException ignored) {}
             }
         }
     }
    */
}
