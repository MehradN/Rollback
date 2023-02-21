package ir.mehradn.rollback.gui;

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
    private BackupManager backupManager;
    private ButtonWidget rollbackButton;

    public RollbackScreen(LevelSummary summary, BooleanConsumer callback) {
        super(Text.translatable("rollback.screen.title"));
        this.callback = callback;
        this.levelSummary = summary;
        this.backupManager = new BackupManager();
    }

    protected void init() {
        this.rollbackButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.rollbackButton"),
                (button) -> System.out.println("ROLLBACK_SCREEN: rollbackButton")
        ).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.manualBackup"), (button) -> {
            boolean b = backupManager.createNormalBackup(this.levelSummary);
            callback.accept(!b);
        }).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.backupFolder"),
                (button) -> Util.getOperatingSystem().open(this.client.getLevelStorage().getBackupsDirectory().toFile())
        ).dimensions(this.width / 2 - 154, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL,
                (button) -> this.callback.accept(false)
        ).dimensions(this.width / 2 + 4, this.height - 28, 150, 20).build());
        this.worldSelected(false);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void worldSelected(boolean active) {
        this.rollbackButton.active = active;
    }
}
