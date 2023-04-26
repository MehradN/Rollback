package ir.mehradn.rollback.rollback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.exception.*;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;
import ir.mehradn.rollback.rollback.metadata.RollbackData;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.minecraft.FileUtil;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommonBackupManager extends BackupManager {
    public static Path rollbackDirectory = null;
    private static Path iconsDirectory = null;
    private final Gofer gofer;
    private EventAnnouncer eventAnnouncer;

    public CommonBackupManager(Gofer gofer) {
        this.gofer = gofer;
        this.eventAnnouncer = new NullEventAnnouncer();
    }

    public static void setPaths(Path rollbackDirectory) {
        CommonBackupManager.rollbackDirectory = rollbackDirectory;
        CommonBackupManager.iconsDirectory = rollbackDirectory.resolve("icons");
    }

    public void setEventAnnouncer(EventAnnouncer eventAnnouncer) {
        this.eventAnnouncer = eventAnnouncer;
    }

    public void loadData()
        throws BackupIOException {
        Rollback.LOGGER.info("Loading the metadata file...");
        Path metadataPath = rollbackDirectory.resolve("rollbacks.json");
        boolean save = false;

        try (FileReader reader = new FileReader(metadataPath.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            MetadataUpdater updater = new MetadataUpdater(json);
            if (updater.getVersion().isOutdated()) {
                updater.update();
                save = true;
            }
            this.data = gson.fromJson(json, RollbackData.class);
        } catch (FileNotFoundException e) {
            Rollback.LOGGER.warn("Metadata file not found! Creating a new one...");
            this.data = new RollbackData();
            save = true;
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to read the metadata file!", e);
            throw new BackupIOException("Failed to read the metadata file!", e);
        }

        if (save)
            saveData();
    }

    public void saveData()
        throws BackupIOException {
        assert rollbackDirectory != null;
        assert iconsDirectory != null;

        Rollback.LOGGER.debug("Saving the metadata file...");
        Path metadataPath = rollbackDirectory.resolve("rollbacks.json");
        try {
            Files.createDirectories(rollbackDirectory);
            Files.createDirectories(iconsDirectory);
            try (FileWriter writer = new FileWriter(metadataPath.toFile())) {
                gson.toJson(this.data, writer);
            }
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the metadata file!", e);
            throw new BackupIOException("Failed to save the metadata file!", e);
        }
    }

    public void updateWorld(String levelID, RollbackWorld world)
        throws BackupIOException {
        this.data.worlds.put(levelID, world);
        saveData();
    }

    public void deleteWorld(String levelID)
        throws BackupIOException {
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", levelID);
        RollbackWorld world = this.data.getWorld(levelID);

        Set<Integer> automatedIDs = world.automatedBackups.keySet();
        Set<Integer> commandIDs = world.commandBackups.keySet();
        while (!automatedIDs.isEmpty())
            deleteBackup(levelID, automatedIDs.iterator().next(), RollbackBackupType.AUTOMATED);
        while (!commandIDs.isEmpty())
            deleteBackup(levelID, commandIDs.iterator().next(), RollbackBackupType.COMMAND);

        this.data.worlds.remove(levelID);
        saveData();
    }

    public void deleteBackup(String levelID, int backupID, RollbackBackupType type)
        throws BackupIOException {
        Rollback.LOGGER.info("Deleting the backup #{} type {}...", backupID, type.toString());
        RollbackWorld world = this.data.getWorld(levelID);
        Map<Integer, RollbackBackup> backups = world.getBackups(type);
        RollbackBackup backup = world.getBackup(backupID, type);

        Rollback.LOGGER.debug("Deleting the backup files...");
        try {
            if (backup.backupPath != null)
                Files.deleteIfExists(rollbackDirectory.resolve(backup.backupPath));
            if (backup.iconPath != null)
                Files.deleteIfExists(rollbackDirectory.resolve(backup.iconPath));
        } catch (IOException e) {
            showError("rollback.deleteBackup.failed", "Failed to delete the backup files!", e, BackupIOException::new);
            return;
        }

        backups.remove(backupID);
        saveData();
    }

    public void createNormalBackup(String name)
        throws BackupIOException {
        Rollback.LOGGER.info("Creating a normal backup...");
        try {
            BackupInfo backupInfo = this.gofer.makeBackup();
            this.eventAnnouncer.onSuccessfulBackup(backupInfo.size());
        } catch (IOException e) {
            showError("selectWorld.edit.backupFailed", "Failed to create a normal backup!", e, BackupIOException::new);
        }
    }

    public void createSpecialBackup(String levelID, String name, RollbackBackupType type)
        throws BackupMinecraftException, BackupIOException {
        Rollback.LOGGER.info("Creating a rollback backup...");
        RollbackWorld world = this.data.getWorld(levelID);
        Map<Integer, RollbackBackup> backups = world.getBackups(type);
        int id = world.lastID + 1;

        while (backups.size() >= RollbackConfig.maxBackupsPerWorld(type))
            deleteBackup(levelID, Collections.min(backups.keySet()), type);
        deleteGhostIcons(levelID);

        Rollback.LOGGER.debug("Saving the world...");
        try {
            this.gofer.saveEverything();
        } catch (MinecraftException e) {
            showError("rollback.createBackup.failed", "Failed to save the world!", e, BackupMinecraftException::new);
            return;
        }

        Rollback.LOGGER.debug("Creating a backup...");
        BackupInfo backupInfo;
        try {
            backupInfo = this.gofer.makeBackup();
            this.eventAnnouncer.onSuccessfulBackup(backupInfo.size());
        } catch (IOException e) {
            showError("rollback.createBackup.failed", "Failed to create a backup!", e, BackupIOException::new);
            return;
        }

        Rollback.LOGGER.debug("Moving the backup...");
        Path path1 = backupInfo.backupPath();
        Path path2;
        try {
            path2 = rollbackDirectory.resolve(FileUtil.findAvailableName(rollbackDirectory, levelID + "_" + id, ".zip"));
            Files.move(path1, path2);
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to move the backup file!", e);

            Rollback.LOGGER.info("Deleting the backup file...");
            try {
                Files.deleteIfExists(path1);
            } catch (IOException ignored) { }

            this.eventAnnouncer.onError("rollback.createBackup.failed", "Failed to move the backup file!");
            throw new BackupIOException("Failed to move the backup file!", e);
        }

        Rollback.LOGGER.debug("Adding the metadata...");
        path2 = rollbackDirectory.relativize(path2);
        RollbackBackup backup = new RollbackBackup();
        backup.backupPath = path2;
        backup.creationDate = LocalDateTime.now();
        backup.daysPlayed = this.gofer.getDaysPlayed();
        backup.name = name;
        backups.put(id, backup);
        world.lastID++;
        saveData();
    }

    public void rollbackToBackup(String levelID, int backupID, RollbackBackupType type)
        throws BackupIOException {
        RollbackBackup backup = this.data.getWorld(levelID).getBackup(backupID, type);
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());

        Rollback.LOGGER.debug("Deleting the current save...");
        try {
            this.gofer.deleteLevel();
        } catch (IOException e) {
            showError("rollback.rollbackToBackup.failed", "Failed to delete the current save!", e, BackupIOException::new);
            return;
        }

        Rollback.LOGGER.debug("Extracting the backup to save directory...");
        Path source = rollbackDirectory.resolve(backup.backupPath.toString());
        Path dest = this.gofer.getSaveDirectory();
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
            showError("rollback.rollbackToBackup.failed", "Failed to extract the backup to the save directory!", e, BackupIOException::new);
        }
    }

    private void deleteGhostIcons(String levelID) {
        RollbackWorld world = this.data.getWorld(levelID);
        for (RollbackBackup backup : world.automatedBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
        for (RollbackBackup backup : world.commandBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
    }

    private <EX extends BackupManagerException> void showError(String title, String info, Throwable cause, ExceptionBuilder<EX> builder)
        throws EX {
        Rollback.LOGGER.error(info, cause);
        this.eventAnnouncer.onError(title, info);
        throw builder.build(info, cause);
    }
}
