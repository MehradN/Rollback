package ir.mehradn.rollback.gui;

import ir.mehradn.rollback.gui.widget.RollbackListWidget;
import ir.mehradn.rollback.util.backup.BackupManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
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

    protected void init() {
        this.rollbackList = new RollbackListWidget(this, this.backupManager, this.levelSummary, this.client,
                this.width, this.height, 22, this.height - 64, 36);
        this.addSelectableChild(this.rollbackList);

        this.rollbackButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.rollbackButton"),
                (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.RollbackEntry::play)
        ).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.manualBackup"), (button) -> {
            boolean b = backupManager.createNormalBackup(this.levelSummary);
            callback.accept(!b);
        }).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.delete"),
                (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.RollbackEntry::delete)
        ).dimensions(this.width / 2 - 154, this.height - 28, 72, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.openFolder"),
                (button) -> Util.getOperatingSystem().open(this.client.getLevelStorage().getBackupsDirectory().toFile())
        ).dimensions(this.width / 2 - 76, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL,
                (button) -> this.callback.accept(false)
        ).dimensions(this.width / 2 + 82, this.height - 28, 72, 20).build());
        this.rollbackSelected(false);
    }

    public void closeAndReload() {
        this.callback.accept(true);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.rollbackList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void rollbackSelected(boolean active) {
        this.rollbackButton.active = active;
        this.deleteButton.active = active;
    }

    public void removed() {
        if (this.rollbackList != null)
            this.rollbackList.children().forEach(RollbackListWidget.RollbackEntry::close);
    }
}
