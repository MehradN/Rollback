package ir.mehradn.rollback.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.config.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class AbstractConfigScreen extends Screen {
    protected List<ConfigWidget> configWidgets;
    private final Runnable onClose;
    private MultiLineTextWidget hoverText;

    protected AbstractConfigScreen(Component title, Runnable onClose) {
        super(title);
        this.onClose = onClose;
    }

    @Override
    public void init() {
        List<WidgetBuilder<?>> widgetBuilders = generateWidgets();
        this.configWidgets = new ArrayList<>();
        for (int i = 0; i < widgetBuilders.size(); i++) {
            int x = this.width / 2 - 127;
            int y = 35 + i * 24;
            WidgetHolder<?> widget = new WidgetHolder<>(widgetBuilders.get(i).build(x, y, 200, 20));
            addRenderableWidget(widget.get());

            final ConfigWidget configWidget = widget.get();
            final Button resetButton = Button.builder(Component.translatable("controls.reset"), (btn) -> configWidget.reset())
                .bounds(x + 204, y, 50, 20)
                .tooltip(Tooltip.create(Component.translatable("rollback.screen.tooltip.resetConfig")))
                .build();
            resetButton.active = configWidget.isNotDefault();
            configWidget.onChange(() -> resetButton.active = configWidget.isNotDefault());
            this.configWidgets.add(configWidget);
            addRenderableWidget(resetButton);
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (btn) -> onClose())
            .bounds(this.width / 2 - 102, this.height - 35, 100, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> onSave())
            .bounds(this.width / 2 + 2, this.height - 35, 100, 20).build());

        this.hoverText = addRenderableWidget(new MultiLineTextWidget(this.width / 2 - 150, descriptionHeight(), Component.empty(), this.font)
            .setMaxWidth(300));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Component hoverText = Component.empty();
        for (int i = 0; i < this.configWidgets.size(); i++) {
            ConfigWidget configWidget = this.configWidgets.get(i);
            int min_y = 35 + i * 24;
            int max_y = min_y + 24;
            if (min_y <= mouseY && mouseY < max_y)
                hoverText = configWidget.onHover();
        }
        this.hoverText.setMessage(hoverText);

        renderDirtBackground(poseStack);
        ControlsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }

    protected static WidgetBuilder<?> booleanEntry(ConfigEntry.Boolean entry) {
        return (x, y, w, h) -> new ConfigBooleanWidget(x, y, w, h, entry);
    }

    protected static WidgetBuilder<?> shortEntry(ConfigEntry.Short entry) {
        return (x, y, w, h) -> new ConfigSliderWidget(x, y, w, h, entry);
    }

    protected static <T extends Enum<T>> WidgetBuilder<?> enumEntry(ConfigEntry.Enum<T> entry) {
        return (x, y, w, h) -> new ConfigEnumWidget<>(x, y, w, h, entry);
    }

    protected abstract List<WidgetBuilder<?>> generateWidgets();

    protected abstract int descriptionHeight();

    protected abstract void onSave();

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface WidgetBuilder <T extends AbstractWidget & ConfigWidget> {
        T build(int x, int y, int w, int h);
    }

    @Environment(EnvType.CLIENT)
    private record WidgetHolder <T extends AbstractWidget & ConfigWidget>(T widget) {
        public T get() {
            return this.widget;
        }
    }
}
