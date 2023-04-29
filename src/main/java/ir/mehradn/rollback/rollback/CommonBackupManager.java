package ir.mehradn.rollback.rollback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.exception.*;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackData;
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
    public EventAnnouncer eventAnnouncer;
    private final Gofer gofer;
    private final Path rollbackDirectory;
    private final Path iconsDirectory;
    private final Path saveDirectory;
    private RollbackData data;

    public CommonBackupManager(Gofer gofer) {
        this.gofer = gofer;
        this.eventAnnouncer = new NullEventAnnouncer();
        this.rollbackDirectory = this.gofer.getBackupDirectory().resolve("rollbacks");
        this.iconsDirectory = this.rollbackDirectory.resolve("icons");
        this.saveDirectory = this.gofer.getSaveDirectory();
    }

    public void loadData()
        throws BackupIOException {
        Rollback.LOGGER.info("Loading the metadata file...");
        Path metadataPath = this.rollbackDirectory.resolve("rollbacks.json");
        boolean save = false;

        try (FileReader reader = new FileReader(metadataPath.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            MetadataUpdater updater = new MetadataUpdater(json);
            if (updater.getVersion().isOutdated()) {
                updater.update();
                save = true;
            }
            this.data = GSON.fromJson(json, RollbackData.class);
        } catch (FileNotFoundException e) {
            Rollback.LOGGER.warn("Metadata file not found! Creating a new one...");
            this.data = new RollbackData();
            save = true;
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to read the metadata file!", e);
            throw new BackupIOException("Failed to read the metadata file!", e);
        }

        this.world = this.data.getWorld(this.gofer.getLevelID());
        if (save)
            saveData();
    }

    public void saveData()
        throws BackupIOException {
        Rollback.LOGGER.debug("Saving the metadata file...");
        Path metadataPath = this.rollbackDirectory.resolve("rollbacks.json");
        try {
            Files.createDirectories(this.rollbackDirectory);
            Files.createDirectories(this.iconsDirectory);
            try (FileWriter writer = new FileWriter(metadataPath.toFile())) {
                GSON.toJson(this.data, writer);
            }
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the metadata file!", e);
            throw new BackupIOException("Failed to save the metadata file!", e);
        }
    }

    public void updateWorld()
        throws BackupIOException {
        saveData();
    }

    public void deleteWorld()
        throws BackupIOException {
        String levelID = this.gofer.getLevelID();
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", levelID);

        Set<Integer> automatedIDs = this.world.automatedBackups.keySet();
        Set<Integer> commandIDs = this.world.commandBackups.keySet();
        while (!automatedIDs.isEmpty())
            deleteBackup(automatedIDs.iterator().next(), BackupType.AUTOMATED);
        while (!commandIDs.isEmpty())
            deleteBackup(commandIDs.iterator().next(), BackupType.COMMAND);

        this.data.worlds.remove(levelID);
        saveData();
    }

    public void deleteBackup(int backupID, BackupType type)
        throws BackupIOException {
        Rollback.LOGGER.info("Deleting the backup #{} type {}...", backupID, type.toString());
        Map<Integer, RollbackBackup> backups = this.world.getBackups(type);
        RollbackBackup backup = this.world.getBackup(backupID, type);

        Rollback.LOGGER.debug("Deleting the backup files...");
        try {
            if (backup.backupPath != null)
                Files.deleteIfExists(this.rollbackDirectory.resolve(backup.backupPath));
            if (backup.iconPath != null)
                Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
            this.eventAnnouncer.onSuccessfulDelete();
        } catch (IOException e) {
            showError("rollback.deleteBackup.failed", "Failed to delete the backup files!", e, BackupIOException::new);
            return;
        }

        backups.remove(backupID);
        saveData();
    }

    public void createNormalBackup()
        throws BackupIOException {
        Rollback.LOGGER.info("Creating a normal backup...");
        try {
            BackupInfo backupInfo = this.gofer.makeBackup();
            this.eventAnnouncer.onSuccessfulBackup(backupInfo.size());
        } catch (IOException e) {
            showError("selectWorld.edit.backupFailed", "Failed to create a normal backup!", e, BackupIOException::new);
        }
    }

    public void createSpecialBackup(String name, BackupType type)
        throws BackupMinecraftException, BackupIOException {
        if (name != null && (name.isBlank() || name.length() > MAX_NAME_LENGTH))
            throw new IllegalArgumentException("Backup name is too long");

        Rollback.LOGGER.info("Creating a rollback backup...");
        String levelID = this.gofer.getLevelID();
        Map<Integer, RollbackBackup> backups = this.world.getBackups(type);
        int id = this.world.lastID + 1;

        while (backups.size() >= RollbackConfig.maxBackupsPerWorld(type))
            deleteBackup(Collections.min(backups.keySet()), type);
        deleteGhostIcons();

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
            path2 = this.rollbackDirectory.resolve(FileUtil.findAvailableName(this.rollbackDirectory, levelID + "_" + id, ".zip"));
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
        path2 = this.rollbackDirectory.relativize(path2);
        RollbackBackup backup = new RollbackBackup();
        backup.backupPath = path2;
        backup.creationDate = LocalDateTime.now();
        backup.daysPlayed = this.gofer.getDaysPlayed();
        backup.name = name;
        backups.put(id, backup);
        this.world.lastID++;
        saveData();
    }

    public void rollbackToBackup(int backupID, BackupType type)
        throws BackupIOException {
        RollbackBackup backup = this.world.getBackup(backupID, type);
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());

        Rollback.LOGGER.debug("Deleting the current save...");
        try {
            this.gofer.deleteLevel();
        } catch (IOException e) {
            showError("rollback.rollbackToBackup.failed", "Failed to delete the current save!", e, BackupIOException::new);
            return;
        }

        Rollback.LOGGER.debug("Extracting the backup to save directory...");
        Path source = this.rollbackDirectory.resolve(backup.backupPath.toString());
        Path dest = this.saveDirectory;
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

    private void deleteGhostIcons() {
        for (RollbackBackup backup : this.world.automatedBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(this.rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
        for (RollbackBackup backup : this.world.commandBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(this.rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
    }

    private <EX extends BackupManagerException> void showError(String title, String info, Throwable cause, ExceptionBuilder<EX> builder)
        throws EX {
        Rollback.LOGGER.error(info, cause);
        this.eventAnnouncer.onError(title, info);
        throw builder.build(info, cause);
    }
}
