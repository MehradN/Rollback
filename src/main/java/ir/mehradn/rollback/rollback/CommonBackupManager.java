package ir.mehradn.rollback.rollback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BMECause;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackData;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import net.minecraft.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class CommonBackupManager implements BackupManager {
    @NotNull public EventAnnouncer eventAnnouncer = new NullEventAnnouncer();
    private final Gofer gofer;
    private final Path backupDirectory;
    private final Path rollbackDirectory;
    private final Path iconsDirectory;
    private final Path saveDirectory;
    @Nullable private RollbackData data = null;
    @Nullable private RollbackWorld world = null;

    public CommonBackupManager(Gofer gofer) {
        this.gofer = gofer;
        this.backupDirectory = gofer.getBackupDirectory();
        this.rollbackDirectory = this.backupDirectory.resolve("rollbacks");
        this.iconsDirectory = this.rollbackDirectory.resolve("icons");
        this.saveDirectory = this.gofer.getSaveDirectory();
    }

    @Override
    public @NotNull RollbackWorld getWorld() {
        Assertion.state(this.world != null, "Call loadWorld before this!");
        return this.world;
    }

    @Override
    public void loadWorld() throws BackupManagerException {
        Rollback.LOGGER.info("Loading the metadata file...");
        Path metadataPath = this.rollbackDirectory.resolve("rollbacks.json");
        boolean save = false;

        RollbackData data;
        try (FileReader reader = new FileReader(metadataPath.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            MetadataUpdater updater = new MetadataUpdater(json);
            if (updater.getVersion().isOutdated()) {
                updater.update();
                save = true;
            }
            data = GSON.fromJson(json, RollbackData.class);
        } catch (FileNotFoundException e) {
            Rollback.LOGGER.warn("Metadata file not found! Creating a new one...");
            data = new RollbackData();
            save = true;
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to read the metadata file!", e);
            throw new BackupManagerException(BMECause.IO_EXCEPTION, "Failed to read the metadata file!", e);
        }

        this.data = data;
        this.world = data.getWorld(this.gofer.getLevelID());
        this.world.config.setBackupManager(this);
        if (save)
            saveWorld();
    }

    @Override
    public void saveWorld() throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
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
            throw new BackupManagerException(BMECause.IO_EXCEPTION, "Failed to save the metadata file!", e);
        }
    }

    @Override
    public void deleteWorld() throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        String levelID = this.gofer.getLevelID();
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", levelID);

        Set<Integer> automatedIDs = this.world.automatedBackups.keySet();
        Set<Integer> commandIDs = this.world.commandBackups.keySet();
        while (!automatedIDs.isEmpty())
            deleteBackup(automatedIDs.iterator().next(), BackupType.AUTOMATED);
        while (!commandIDs.isEmpty())
            deleteBackup(commandIDs.iterator().next(), BackupType.COMMAND);

        this.data.worlds.remove(levelID);
        saveWorld();
    }

    @Override
    public void createBackup(String name, BackupType type) throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        if (name != null && (!type.list || name.isBlank()))
            name = null;
        Assertion.argument(name == null || name.length() <= MAX_NAME_LENGTH, "Backup name is too long");
        Rollback.LOGGER.info("Creating a {} backup...", type);
        String levelID = this.gofer.getLevelID();

        if (type.automatedDeletion) {
            Map<Integer, RollbackBackup> backups = this.world.getBackups(type);
            while (backups.size() >= this.world.config.getMaxBackupsForType(type))
                deleteBackup(Collections.min(backups.keySet()), type);
            deleteGhostIcons();
        }

        Rollback.LOGGER.debug("Saving the world...");
        try {
            this.gofer.saveEverything();
        } catch (BackupManagerException e) {
            throw showError("rollback.error.backupCreation", "Failed to save the world!", e);
        }

        Rollback.LOGGER.debug("Creating the backup...");
        BackupInfo backupInfo;
        try {
            backupInfo = this.gofer.makeBackup();
            this.eventAnnouncer.onSuccessfulBackup(backupInfo.size());
        } catch (BackupManagerException e) {
            throw showError("rollback.error.backupCreation", "Failed to create a backup!", e);
        }

        if (!type.list)
            return;
        int id = this.world.lastID + 1;

        Rollback.LOGGER.debug("Moving the backup...");
        Path path1 = backupInfo.backupPath();
        Path path2;
        try {
            path2 = this.rollbackDirectory.resolve(FileUtil.findAvailableName(this.rollbackDirectory, levelID + "_" + id, ".zip"));
            Files.move(path1, path2);
        } catch (IOException e) {
            BackupManagerException exception =
                showError("rollback.error.backupCreation", "Failed to move the backup file!", BMECause.IO_EXCEPTION, e);

            Rollback.LOGGER.debug("Deleting the backup file...");
            try {
                Files.deleteIfExists(path1);
            } catch (IOException ignored) { }

            throw exception;
        }

        Rollback.LOGGER.debug("Adding the metadata...");
        path2 = this.rollbackDirectory.relativize(path2);
        RollbackBackup backup = new RollbackBackup();
        backup.backupPath = path2;
        backup.creationDate = LocalDateTime.now();
        backup.daysPlayed = this.gofer.getDaysPlayed();
        backup.name = name;
        this.world.getBackups(type).put(id, backup);
        this.world.lastID++;
        saveWorld();
    }

    @Override
    public void deleteBackup(int backupID, BackupType type) throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        Assertion.argument(type.automatedDeletion, "Invalid type!");
        Rollback.LOGGER.info("Deleting the backup #{} type {}...", backupID, type);
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
            throw showError("rollback.error.backupDeletion", "Failed to delete the backup files!", BMECause.IO_EXCEPTION, e);
        }

        backups.remove(backupID);
        saveWorld();
    }

    @Override
    public void convertBackup(int backupID, BackupType from, String name, BackupType to) throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        Assertion.argument(from.convertFrom && to.convertTo && from != to, "Invalid types!");
        if (name != null && (!to.list || name.isBlank()))
            name = null;
        Assertion.argument(name == null || name.length() <= MAX_NAME_LENGTH, "Backup name is too long");

        Rollback.LOGGER.info("Converting the backup #{} from {} to {}", backupID, from, to);
        RollbackBackup backup = this.world.getBackup(backupID, from);

        if (to == BackupType.MANUAL) {
            if (backup.iconPath != null) {
                Rollback.LOGGER.debug("Deleting the icon...");
                try {
                    Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
                } catch (IOException e) {
                    throw showError("rollback.error.backupConversion", "Failed to delete the icon!", BMECause.IO_EXCEPTION, e);
                }
            }
            if (backup.backupPath != null) {
                Rollback.LOGGER.debug("Moving the backup file...");
                try {
                    String fileName = LocalDateTime.now().format(LocalDateTimeAdapter.TIME_FORMATTER) + "_" + this.gofer.getLevelID();
                    Path source = this.rollbackDirectory.resolve(backup.backupPath);
                    Path dest = this.backupDirectory.resolve(FileUtil.findAvailableName(this.backupDirectory, fileName, ".zip"));
                    Files.move(source, dest);
                } catch (IOException e) {
                    throw showError("rollback.error.backupConversion", "Failed to move the backup files!", BMECause.IO_EXCEPTION, e);
                }
            }
        }
        this.eventAnnouncer.onSuccessfulConvert(from, to);

        Rollback.LOGGER.debug("Updating the metadata,,,");
        this.world.getBackups(from).remove(backupID);
        if (to.list) {
            int id = ++this.world.lastID;
            backup.name = name;
            this.world.getBackups(to).put(id, backup);
        }
        saveWorld();
    }

    @Override
    public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        Assertion.argument(type.rollback, "Invalid type!");
        RollbackBackup backup = this.world.getBackup(backupID, type);
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());

        Rollback.LOGGER.debug("Deleting the current save...");
        try {
            this.gofer.deleteLevel();
        } catch (BackupManagerException e) {
            throw showError("rollback.error.rollbackToBackup", "Failed to delete the current save!", e);
        }

        Rollback.LOGGER.debug("Extracting the backup to save directory...");
        Path source = this.rollbackDirectory.resolve(backup.backupPath.toString());
        try (ZipFile zip = new ZipFile(source.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = this.saveDirectory.resolve(entry.getName());
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
            throw showError("rollback.error.rollbackToBackup", "Failed to extract the backup to the save directory!",
                BMECause.IO_EXCEPTION, e);
        }
    }

    private void deleteGhostIcons() {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        for (RollbackBackup backup : this.world.automatedBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(this.rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
        for (RollbackBackup backup : this.world.commandBackups.values())
            if (backup.iconPath != null && !Files.isRegularFile(this.rollbackDirectory.resolve(backup.iconPath)))
                backup.iconPath = null;
    }

    private BackupManagerException showError(String title, String info, BMECause cause, Throwable exception) {
        Rollback.LOGGER.error(info, exception);
        this.eventAnnouncer.onError(title, info);
        return new BackupManagerException(cause, info, exception);
    }

    private BackupManagerException showError(String title, String info, BackupManagerException cause) {
        Rollback.LOGGER.error(info, cause);
        this.eventAnnouncer.onError(title, info);
        return new BackupManagerException(info, cause);
    }
}
