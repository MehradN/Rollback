package ir.mehradn.rollback.gui;

import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.util.screen.TexturedOverlayButtonWidget;
import ir.mehradn.rollback.gui.widget.RollbackListWidget;
import ir.mehradn.rollback.util.backup.BackupManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;


@Environment(EnvType.CLIENT)
public class RollbackScreen extends Screen {
    private static final Identifier MIDNIGHTLIB_ICON_TEXTURE = new Identifier("midnightlib","textures/gui/midnightlib_button.png");
    private final BooleanConsumer callback;
    private final LevelSummary levelSummary;
    private final BackupManager backupManager;
    private final Screen configScreen;
    private RollbackListWidget rollbackList;
    private ButtonWidget rollbackButton;
    private ButtonWidget deleteButton;

    public RollbackScreen(LevelSummary summary, BooleanConsumer callback) {
        super(Text.translatable("rollback.screen.title"));
        this.callback = callback;
        this.levelSummary = summary;
        this.backupManager = new BackupManager();
        this.configScreen = MidnightConfig.getScreen(this, "rollback");
    }

    protected void init() {
        this.rollbackList = new RollbackListWidget(
            this, this.backupManager, this.levelSummary, this.client,
            this.width, this.height, 22, this.height - 64, 36
        );
        addSelectableChild(this.rollbackList);

        this.rollbackButton = addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.rollbackButton"),
            (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.RollbackEntry::play)
        ).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.manualBackup"),
            (button) -> {
                boolean b = this.backupManager.createNormalBackup(this.levelSummary);
                this.callback.accept(!b);
            }
        ).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.deleteButton = addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.delete"),
            (button) -> this.rollbackList.getSelectedAsOptional().ifPresent(RollbackListWidget.RollbackEntry::delete)
        ).dimensions(this.width / 2 - 154, this.height - 28, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.openFolder"),
            (button) -> Util.getOperatingSystem().open(this.client.getLevelStorage().getBackupsDirectory().toFile())
        ).dimensions(this.width / 2 - 76, this.height - 28, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.screen.cancel"),
            (button) -> this.callback.accept(false)
        ).dimensions(this.width / 2 + 82, this.height - 28, 72, 20).build());

        this.addDrawableChild(new TexturedOverlayButtonWidget(
            this.width - 22, 1, 20, 20, 0, 0, 20,
            MIDNIGHTLIB_ICON_TEXTURE, 32, 64,
            (button) -> this.client.setScreen(this.configScreen)
        ));

        rollbackSelected(false);
    }

    public void closeAndReload() {
        this.callback.accept(true);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
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
