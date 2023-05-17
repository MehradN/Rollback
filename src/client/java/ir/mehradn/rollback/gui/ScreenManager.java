package ir.mehradn.rollback.gui;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ScreenManager {
    public final BackupManager backupManager;
    @Nullable private static ScreenManager instance = null;
    private final Minecraft client;
    private final Screen lastScreen;
    private final RollbackScreen rollbackScreen;

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
            this.backupManager.createBackup(name, type);
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

    // TODO
    public void renameBackup(int backupID, BackupType type) {
        setMessageScreen(Component.translatable("rollback.screen.message.renaming"));
    }

    // TODO
    public void convertBackup(int backupID, BackupType from) {
        setMessageScreen(Component.translatable("rollback.screen.message.converting"));
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

    // TODO
    public void openBackupFolder() {
    }

    public void onError(String translatableTitle, String literalInfo) {
        setErrorScreen(Component.translatable(translatableTitle), Component.literal(literalInfo));
    }

    // TODO
    public void onSuccess() {

    }

    // TODO
    public void onTick() {
        BackupManager.State state = this.backupManager.getCurrentState();
        if (state == BackupManager.State.INITIAL)
            loadMetadata();
        if (state == BackupManager.State.IDLE && this.client.screen != this.rollbackScreen)
            this.client.setScreen(this.rollbackScreen);
    }

    // TODO
    public void onChange() {

    }

    // TODO
    private void setErrorScreen(Component title, Component message) {

    }

    // TODO
    private void setMessageScreen(Component message) {

    }
}
