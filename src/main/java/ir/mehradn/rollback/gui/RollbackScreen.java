package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.midnightdust.lib.config.MidnightConfig;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackWorld;
import ir.mehradn.rollback.util.mixin.WorldSelectionListCallbackAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class RollbackScreen extends Screen {
    private final Consumer<WorldSelectionListCallbackAction> callback;
    private final LevelSummary levelSummary;
    private final BackupManager backupManager;
    private final RollbackWorld rollbackWorld;
    private RollbackSelectionList rollbackList;
    private Button rollbackButton;
    private Button deleteButton;

    public RollbackScreen(LevelSummary summary, Consumer<WorldSelectionListCallbackAction> callback) {
        super(Component.translatable("rollback.screen.title"));
        this.callback = callback;
        this.levelSummary = summary;
        this.backupManager = BackupManager.loadMetadata();
        this.rollbackWorld = this.backupManager.getWorld(this.levelSummary.getLevelId());
    }

    public void doAction(WorldSelectionListCallbackAction action) {
        this.callback.accept(action);
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        this.rollbackList.render(poseStack, mouseX, mouseY, delta);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    public void setEntrySelected(boolean playActive, boolean deleteActive) {
        this.rollbackButton.active = playActive;
        this.deleteButton.active = deleteActive;
    }

    public void removed() {
        if (this.rollbackList != null)
            this.rollbackList.children().forEach(RollbackSelectionList.Entry::close);
    }

    protected void init() {
        assert this.minecraft != null;

        this.rollbackList = new RollbackSelectionList(
            this, this.backupManager, this.rollbackWorld, this.levelSummary, this.minecraft,
            this.width, this.height, 22, this.height - 84, 36
        );
        addWidget(this.rollbackList);

        this.rollbackButton = addRenderableWidget(Button.builder(
            Component.translatable("rollback.screen.rollbackButton"),
            (button) -> this.rollbackList.getSelectedOpt().ifPresent(RollbackSelectionList.Entry::playBackup)
        ).bounds(this.width / 2 - 154, this.height - 76, 150, 20).build());
        addRenderableWidget(Button.builder(
            Component.translatable("rollback.screen.manualBackup"),
            (button) -> {
                Rollback.LOGGER.info("Creating a manual backup...");
                boolean b = this.backupManager.createNormalBackup(this.levelSummary);
                if (!b)
                    this.callback.accept(WorldSelectionListCallbackAction.RELOAD_WORLD_LIST);
            }
        ).bounds(this.width / 2 + 4, this.height - 76, 150, 20).build());

        addRenderableWidget(CycleButton.onOffBuilder(this.rollbackWorld.automatedBackups).create(
            this.width / 2 - 154, this.height - 52, 150, 20,
            Component.translatable("rollback.screen.automatedOption"),
            (button, enabled) -> {
                this.rollbackWorld.automatedBackups = enabled;
                this.backupManager.saveMetadata();
            }
        ));
        addRenderableWidget(Button.builder(
            Component.translatable("selectWorld.edit.backupFolder"),
            (button) -> {
                LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
                Path path = levelStorageSource.getBackupPath();

                try {
                    FileUtil.createDirectoriesSafe(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Util.getPlatform().openFile(path.toFile());
            }
        ).bounds(this.width / 2 + 4, this.height - 52, 150, 20).build());

        this.deleteButton = addRenderableWidget(Button.builder(
            Component.translatable("selectWorld.deleteButton"),
            (button) -> this.rollbackList.getSelectedOpt().ifPresent(RollbackSelectionList.Entry::deleteBackup)
        ).bounds(this.width / 2 - 154, this.height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(
            Component.translatable("menu.options"),
            (button) -> this.minecraft.setScreen(MidnightConfig.getScreen(this, Rollback.MOD_ID))
        ).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(
            Component.translatable("gui.cancel"),
            (button) -> this.callback.accept(WorldSelectionListCallbackAction.NOTHING)
        ).bounds(this.width / 2 + 54, this.height - 28, 100, 20).build());

        setEntrySelected(false, false);
    }
}
