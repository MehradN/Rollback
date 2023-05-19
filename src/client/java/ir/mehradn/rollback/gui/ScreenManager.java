package ir.mehradn.rollback.gui;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public final class ScreenManager {
    public final BackupManager backupManager;
    @Nullable private static ScreenManager instance = null;
    private final Minecraft client;
    private final Screen lastScreen;
    private final RollbackScreen rollbackScreen;
    private boolean onInputScreen = false;

    private ScreenManager(Minecraft client, BackupManager backupManager) {
        this.backupManager = backupManager;
        this.client = client;
        this.lastScreen = client.screen;
        this.rollbackScreen = new RollbackScreen();
    }

    public static @Nullable ScreenManager getInstance() {
        return instance;
    }

    public static void activate(Minecraft client, BackupManager backupManager) {
        if (instance != null)
            deactivate();
        instance = new ScreenManager(client, backupManager);
    }

    public static void deactivate() {
        if (instance == null)
            return;
        instance.client.setScreen(instance.lastScreen);
        instance = null;
    }

    public void loadMetadata() {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.loading"));
            this.backupManager.loadWorld();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to load the metadata!", e);
        }
    }

    public void createBackup(String name, BackupType type) {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.creating"));
            this.backupManager.createBackup(type, name);
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to create a new backup!", e);
        }
    }

    public void deleteBackup(int backupID, BackupType type) {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.deleting"));
            this.backupManager.deleteBackup(backupID, type);
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to delete the backup!", e);
        }
    }

    public void renameBackup(int backupID, BackupType type) {
        this.onInputScreen = true;
        this.client.setScreen(new RenameScreen(
            this.backupManager.getWorld().getBackup(backupID, type).name,
            (renamed, name) -> {
                this.onInputScreen = false;
                if (!renamed)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.screen.message.renaming"));
                    this.backupManager.renameBackup(backupID, type, name);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to rename the backup!", e);
                }
            }));
    }

    public void convertBackup(int backupID, BackupType from) {
        this.onInputScreen = true;
        this.client.setScreen(new ConvertScreen(from,
            (converted, to) -> {
                this.onInputScreen = false;
                if (!converted)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.screen.message.converting"));
                    this.backupManager.convertBackup(backupID, from, to);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to convert the backup!", e);
                }
            }));
    }

    public void rollbackToBackup(int backupID, BackupType type) {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.rolling"));
            this.backupManager.rollbackToBackup(backupID, type);
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to rollback to the backup!", e);
        }
    }

    // TODO
    public void openConfig() {
    }

    public void saveConfig() {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.savingConfig"));
            this.backupManager.saveConfig();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to save the config!", e);
        }
    }

    public void saveConfigAsDefault() {
        try {
            setMessageScreen(Component.translatable("rollback.screen.message.savingConfig"));
            this.backupManager.saveConfigAsDefault();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to save the config as default!", e);
        }
    }

    public void openBackupFolder() {
        LevelStorageSource levelStorageSource = this.client.getLevelSource();
        Path path = levelStorageSource.getBackupPath();

        try {
            FileUtil.createDirectoriesSafe(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Util.getPlatform().openFile(path.toFile());
    }

    public void onError(String translatableTitle, String literalInfo) {
        this.onInputScreen = true;
        this.client.forceSetScreen(new RollbackErrorScreen(
            Component.translatable(translatableTitle),
            Component.literal(literalInfo),
            () -> this.onInputScreen = false));
    }

    public void onSuccess(Component title, Component info) {
        this.client.getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, title, info));
    }

    public void onTick() {
        if (this.onInputScreen)
            return;

        BackupManager.State state = this.backupManager.getCurrentState();
        if (state == BackupManager.State.INITIAL)
            loadMetadata();
        if (state == BackupManager.State.IDLE && this.client.screen != this.rollbackScreen)
            this.client.setScreen(this.rollbackScreen);
    }

    // TODO
    public void onChange() {
        this.loadMetadata();
    }

    private void setMessageScreen(Component message) {
        this.client.forceSetScreen(new GenericDirtMessageScreen(message));
    }
}
