package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.packets.TakeScreenshot;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.util.Utils;
import ir.mehradn.rollback.util.mixin.GameRendererExpanded;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class ScreenManager {
    @Nullable private static ScreenManager instance = null;
    public final BackupManager backupManager;
    public final RollbackScreen rollbackScreen;
    private final Minecraft minecraft;
    private final Screen lastScreen;
    private boolean onInputScreen = false;

    private ScreenManager(Minecraft minecraft, BackupManager backupManager) {
        this.backupManager = backupManager;
        this.minecraft = minecraft;
        this.lastScreen = minecraft.screen;
        this.rollbackScreen = new RollbackScreen();
    }

    public static @Nullable ScreenManager getInstance() {
        return instance;
    }

    public static void activate(Minecraft minecraft, BackupManager backupManager) {
        if (instance != null)
            deactivate();
        instance = new ScreenManager(minecraft, backupManager);
    }

    public static void deactivate() {
        if (instance == null)
            return;
        instance.minecraft.setScreen(instance.lastScreen);
        instance = null;
    }

    public static Path getRollbackDirectory(Minecraft minecraft) {
        return minecraft.getLevelSource().getBackupPath().resolve("rollbacks");
    }

    public static boolean isInGame(Minecraft minecraft) {
        return (minecraft.level != null);
    }

    public static boolean isIntegrated(Minecraft minecraft) {
        return minecraft.hasSingleplayerServer();
    }

    public static boolean isAutomatedBackupEnabled(BackupManager backupManager) {
        return backupManager.getWorld().config.backupEnabled.get();
    }

    public static boolean isAutomatedBackupEnabled() {
        if (instance == null)
            return false;
        return isAutomatedBackupEnabled(instance.backupManager);
    }

    public static void showToast(Minecraft minecraft, Component title, Component info) {
        minecraft.getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, title, info));
    }

    public static void takeScreenshot(Minecraft minecraft, int backupId, BackupType type, Path iconPath) {
        Rollback.LOGGER.info("Creating an backup icon...");
        Path path = getRollbackDirectory(minecraft).resolve(iconPath);
        if (minecraft.isPaused())
            ((GameRendererExpanded)minecraft.gameRenderer).queueScreenshotWithoutGui(path);
        else
            ((GameRendererExpanded)minecraft.gameRenderer).takeScreenshotWithGui(path);
        ClientPlayNetworking.send(new TakeScreenshot(backupId, type, iconPath));
    }

    public void loadMetadata() {
        try {
            setMessageScreen(Component.translatable("rollback.message.loading"));
            this.backupManager.loadWorld();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to load the metadata!", e);
        }
    }

    public void createBackup() {
        this.onInputScreen = true;
        this.minecraft.setScreen(new NameScreen(
            Component.translatable("rollback.screen.title.createScreen"),
            null, true,
            (submitted, name) -> {
                this.onInputScreen = false;
                if (!submitted)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.message.creating"));
                    this.backupManager.createBackup(BackupType.BACKUP, name);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to create a new manual backup!", e);
                }
            }
        ));
    }

    public void createManualBackup() {
        try {
            setMessageScreen(Component.translatable("rollback.message.creating"));
            this.backupManager.createBackup(BackupType.MANUAL, null);
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to create a new manual backup!", e);
        }
    }

    public void deleteBackup(int backupID, BackupType type) {
        this.onInputScreen = true;
        this.minecraft.setScreen(new DirtConfirmScreen(
            Component.translatable("rollback.confirm.title.delete." + type),
            Component.empty(),
            (confirmed) -> {
                this.onInputScreen = false;
                if (!confirmed)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.message.deleting." + type));
                    this.backupManager.deleteBackup(backupID, type);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to delete the backup!", e);
                }
            }
        ));
    }

    public void renameBackup(int backupID, BackupType type) {
        this.onInputScreen = true;
        this.minecraft.setScreen(new NameScreen(
            Component.translatable("rollback.screen.title.renameScreen"),
            this.backupManager.getWorld().getBackup(backupID, type).name, false,
            (submitted, name) -> {
                this.onInputScreen = false;
                if (!submitted)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.message.renaming." + type));
                    this.backupManager.renameBackup(backupID, type, name);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to rename the backup!", e);
                }
            }
        ));
    }

    public void convertBackup(int backupID, BackupType from) {
        this.onInputScreen = true;
        this.minecraft.setScreen(new ConvertScreen(from,
            (converted, to) -> {
                this.onInputScreen = false;
                if (!converted)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.message.converting." + from));
                    this.backupManager.convertBackup(backupID, from, to);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to convert the backup!", e);
                }
            }
        ));
    }

    public void rollbackToBackup(int backupID, BackupType type) {
        this.onInputScreen = true;
        this.minecraft.setScreen(new DirtConfirmScreen(
            Component.translatable("rollback.confirm.title.rollback." + type),
            Component.translatable("rollback.confirm.info.rollback"),
            (confirmed) -> {
                this.onInputScreen = false;
                if (!confirmed)
                    return;
                try {
                    setMessageScreen(Component.translatable("rollback.message.rolling"));
                    this.backupManager.rollbackToBackup(backupID, type);
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to rollback to the backup!", e);
                }
            }
        ));
    }

    public void openConfig() {
        this.onInputScreen = true;
        this.minecraft.setScreen(WorldConfigScreen.build(
            this.backupManager.getWorld().config,
            () -> this.onInputScreen = false
        ));
    }

    public void saveConfig() {
        try {
            setMessageScreen(Component.translatable("rollback.message.savingConfig"));
            this.backupManager.saveConfig();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to save the config!", e);
        }
    }

    public void saveConfigAsDefault() {
        try {
            setMessageScreen(Component.translatable("rollback.message.savingConfig"));
            this.backupManager.saveConfigAsDefault();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to save the config as default!", e);
        }
    }

    public void openBackupFolder() {
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
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
        Rollback.LOGGER.error(literalInfo);
        this.minecraft.forceSetScreen(new DirtErrorScreen(
            Component.translatable(translatableTitle).withStyle(ChatFormatting.RED),
            Component.literal(literalInfo).withStyle(ChatFormatting.RED),
            () -> {
                if (ScreenManager.instance != null)
                    ScreenManager.instance.onInputScreen = false;
            }
        ));
    }

    public void onWarning(int count, long size) {
        MutableComponent title = Component.translatable("rollback.confirm.title.backupLimit", count, Utils.fileSizeToString(size))
            .withStyle(ChatFormatting.YELLOW);
        MutableComponent info = Component.empty();
        if (count >= 90)
            info.append(Component.translatable("rollback.confirm.info.backupLimit.1", 99));
        info.append(Component.translatable("rollback.confirm.info.backupLimit.2"))
            .withStyle(ChatFormatting.YELLOW);

        this.onInputScreen = true;
        this.minecraft.setScreen(new DirtErrorScreen(
            title,
            info,
            () -> {
                if (ScreenManager.instance != null)
                    ScreenManager.instance.onInputScreen = false;
            }
        ));
    }

    // TODO: Implement this
    public void onNotMatchingVersions() {

    }

    public void onTick() {
        if (this.onInputScreen)
            return;

        BackupManager.State state = this.backupManager.getCurrentState();
        if (state == BackupManager.State.INITIAL)
            loadMetadata();
        if (state == BackupManager.State.IDLE && this.minecraft.screen != this.rollbackScreen)
            this.minecraft.setScreen(this.rollbackScreen);
    }

    // TODO: Implement rest of this
    public Component currentSaveLastPlayed() {
        if (this.minecraft.level != null)
            return Component.translatable("rollback.screen.text.playingNow");
        return Component.empty();
    }

    // TODO: Implement this
    public @Nullable NativeImage loadCurrentSaveIcon() {
        return null;
    }

    // TODO: Implement rest of this
    public void playCurrentSave() {
        if (isInGame(this.minecraft))
            deactivate();
    }

    private void setMessageScreen(Component message) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(message));
    }

    @Environment(EnvType.CLIENT)
    public static class DirtConfirmScreen extends ConfirmScreen {
        public DirtConfirmScreen(Component title, Component info, BooleanConsumer onConfirm) {
            super(onConfirm, title, info);
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            renderDirtBackground(poseStack);
        }
    }
}
