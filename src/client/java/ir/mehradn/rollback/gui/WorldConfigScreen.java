package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.mehradconfig.MehradConfig;
import ir.mehradn.mehradconfig.gui.ConfigScreenBuilder;
import ir.mehradn.mehradconfig.gui.EntryWidgetFactory;
import ir.mehradn.mehradconfig.gui.screen.MehradConfigScreen;
import ir.mehradn.mehradconfig.gui.screen.ResettableConfigScreen;
import ir.mehradn.mehradconfig.gui.widget.ConfigEntryWidget;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WorldConfigScreen extends ResettableConfigScreen {
    private Button saveAsDefaultButton;
    private boolean savingAsDefault = false;

    public WorldConfigScreen(MehradConfig config, ConfigScreenBuilder.ScreenProperties properties, EntryWidgetFactory entryWidgetFactory,
                             Screen parentScreen) {
        super(config, properties, entryWidgetFactory, parentScreen);
    }

    public static MehradConfigScreen build(RollbackWorldConfig config, Runnable onClose) {
        return new ConfigScreenBuilder()
            .setScreenType(WorldConfigScreen::new, ConfigScreenBuilder.DefaultScreens.RESETTABLE)
            .setButtonWidth(100)
            .setDescriptionY((w, h, f) -> h - f.lineHeight * 3 - 64)
            .setOnSave((m, s, p) -> {
                onClose.run();
                if (ScreenManager.getInstance() != null) {
                    if (s instanceof WorldConfigScreen screen && screen.savingAsDefault)
                        ScreenManager.getInstance().saveConfigAsDefault();
                    else
                        ScreenManager.getInstance().saveConfig();
                }
            })
            .setOnCancel((m, s, p) -> onClose.run())
            .buildForInstance(config);
    }

    @Override
    public void init() {
        super.init();
        int width = this.properties.buttonWidth().get(this.width, this.height, this.font) * 2 + 4;
        this.saveAsDefaultButton = addRenderableWidget(Button.builder(
                Component.translatable("rollback.configScreen.button.saveAsDefault"),
                this::saveAsDefault)
            .bounds((this.width - width) / 2, this.height - 59, width, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        boolean inactive = true;
        for (ConfigEntryWidget<?> entryWidget : this.entryWidgets)
            inactive &= entryWidget.entry.isDefault();
        this.saveAsDefaultButton.active = !inactive;
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderDirtBackground(poseStack);
    }

    private void saveAsDefault(Button button) {
        Assertion.runtime(this.minecraft != null);
        final WorldConfigScreen thisScreen = this;
        this.minecraft.setScreen(new ScreenManager.DirtConfirmScreen(
            Component.translatable("rollback.confirm.title.saveAsDefault"),
            Component.translatable("rollback.confirm.info.saveAsDefault"),
            (confirmed) -> {
                if (confirmed) {
                    thisScreen.savingAsDefault = true;
                    thisScreen.saveButton.onPress();
                    thisScreen.savingAsDefault = false;
                } else {
                    this.minecraft.setScreen(thisScreen);
                }
            }
        ));
    }
}
