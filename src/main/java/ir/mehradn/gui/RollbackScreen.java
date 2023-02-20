package ir.mehradn.gui;

import ir.mehradn.util.backup.BackupManager;
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
    protected final Screen parentScreen;
    private final LevelSummary levelSummary;
    private BackupManager backupManager;
    private ButtonWidget rollbackButton;

    public RollbackScreen(Screen parent, LevelSummary summary) {
        super(Text.translatable("rollback.screen.title"));
        this.parentScreen = parent;
        this.levelSummary = summary;
        this.backupManager = new BackupManager();
    }

    protected void init() {
        this.rollbackButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.rollbackButton"),
                (button) -> System.out.println("ROLLBACK_SCREEN: rollbackButton")
        ).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.screen.manualBackup"),
                (button) -> this.backupManager.createNormalBackup(this.levelSummary)
        ).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit.backupFolder"),
                (button) -> Util.getOperatingSystem().open(this.client.getLevelStorage().getBackupsDirectory().toFile())
        ).dimensions(this.width / 2 - 154, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL,
                (button) -> this.client.setScreen(parentScreen)
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
