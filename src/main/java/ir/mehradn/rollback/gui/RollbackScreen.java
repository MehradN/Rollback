package ir.mehradn.rollback.gui;

import eu.midnightdust.lib.config.MidnightConfig;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.util.backup.BackupManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;

@Environment(EnvType.CLIENT)
public class RollbackScreen extends Screen {
    private final BooleanConsumer callback;
    private final LevelSummary levelSummary;
    private final BackupManager backupManager;
    private RollbackListWidget rollbackList;
    private ButtonWidget rollbackButton;
    private ButtonWidget deleteButton;

    public RollbackScreen(LevelSummary summary, BooleanConsumer callback) {
        super(Text.translatable("rollback.screen.title"));
        this.callback = callback;
        this.levelSummary = summary;
        this.backupManager = new BackupManager();
    }

    public void closeAndReload() {
        this.callback.accept(true);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.rollbackList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void setEntrySelected(boolean playActive, boolean deleteActive) {
        this.rollbackButton.active = playActive;
        this.deleteButton.active = deleteActive;
    }

    public void removed() {
        if (this.rollbackList != null)
            this.rollbackList.children().forEach(RollbackListWidget.Entry::close);
    }

    protected void init() {
        this.rollbackList = new RollbackListWidget(
            this, this.backupManager, this.levelSummary, this.client,
            this.width, this.height, 22, this.height - 84, 36
        );
        addSelectableChild(this.rollbackList);

        this.rollbackButton = addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.rollbackButton"),
            (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.Entry::play)
        ).dimensions(this.width / 2 - 154, this.height - 76, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.manualBackup"),
            (button) -> {
                Rollback.LOGGER.info("Creating a manual backup...");
                boolean b = this.backupManager.createNormalBackup(this.levelSummary);
                this.callback.accept(!b);
            }
        ).dimensions(this.width / 2 + 4, this.height - 76, 150, 20).build());

        boolean automatedEnabled = this.backupManager.getAutomated(this.levelSummary.getName());
        addDrawableChild(CyclingButtonWidget.onOffBuilder(automatedEnabled).build(
            this.width / 2 - 154, this.height - 52, 150, 20,
            Text.translatable("rollback.screen.automatedOption"),
            (button, enabled) -> this.backupManager.setAutomated(this.levelSummary.getName(), enabled)
        ));
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.openFolder"),
            (button) -> Util.getOperatingSystem().open(this.client.getLevelStorage().getBackupsDirectory().toFile())
        ).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());

        this.deleteButton = addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.delete"),
            (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.Entry::delete)
        ).dimensions(this.width / 2 - 154, this.height - 28, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.options"),
            (button) -> this.client.setScreen(MidnightConfig.getScreen(this, "rollback"))
        ).dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.cancel"),
            (button) -> this.callback.accept(false)
        ).dimensions(this.width / 2 + 54, this.height - 28, 100, 20).build());

        setEntrySelected(false, false);
    }
}
