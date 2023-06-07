package ir.mehradn.rollback.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.List;

public class WorldConfigScreen extends AbstractConfigScreen {
    private final RollbackWorldConfig originalConfig;
    private final RollbackWorldConfig editedConfig;
    private Button saveAsDefaultButton;

    public WorldConfigScreen(RollbackWorldConfig config, BackupManager backupManager, Runnable onClose) {
        super(Component.translatable("rollback.screen.title.configScreen.world"), onClose);
        this.originalConfig = config;
        this.editedConfig = new RollbackWorldConfig();
        this.editedConfig.update(backupManager);
        this.editedConfig.copyFrom(this.originalConfig);
    }

    @Override
    public void init() {
        super.init();
        this.saveAsDefaultButton = addRenderableWidget(Button.builder(
                Component.translatable("rollback.screen.button.saveAsDefault"),
                (btn) -> saveAsDefault())
            .bounds(this.width / 2 - 102, this.height - 59, 204, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        boolean show = false;
        for (ConfigWidget configWidget : this.configWidgets)
            show |= configWidget.isNotDefault();
        this.saveAsDefaultButton.active = show;
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected List<WidgetBuilder<?>> generateWidgets() {
        return List.of(
            booleanEntry((ConfigEntry.Boolean)this.editedConfig.backupEnabled),
            shortEntry((ConfigEntry.Short)this.editedConfig.maxBackups),
            shortEntry((ConfigEntry.Short)this.editedConfig.backupFrequency),
            enumEntry((ConfigEntry.Enum<RollbackConfig.TimerMode>)this.editedConfig.timerMode)
        );
    }

    @Override
    protected int descriptionHeight() {
        return this.height - 85;
    }

    @Override
    protected void onSave() {
        this.originalConfig.copyFrom(this.editedConfig);
        this.onClose();
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().saveConfig();
    }

    private void saveAsDefault() {
        if (this.minecraft == null)
            return;

        final WorldConfigScreen thisScreen = this;
        this.minecraft.setScreen(new ScreenManager.DirtConfirmScreen(
            Component.translatable("rollback.screen.saveAsDefaultQuestion"),
            Component.translatable("rollback.screen.saveAsDefaultWarning"),
            (confirmed) -> {
                if (confirmed) {
                    this.originalConfig.copyFrom(this.editedConfig);
                    this.onClose();
                    if (ScreenManager.getInstance() != null)
                        ScreenManager.getInstance().saveConfigAsDefault();
                } else {
                    this.minecraft.setScreen(thisScreen);
                }
            }
        ));
    }
}
