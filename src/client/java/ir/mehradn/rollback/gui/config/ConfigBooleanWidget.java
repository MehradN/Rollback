package ir.mehradn.rollback.gui.config;

import ir.mehradn.rollback.config.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ConfigBooleanWidget extends AbstractButton implements ConfigWidget {
    private final ConfigEntry.Boolean entry;
    private Runnable onChange;

    ConfigBooleanWidget(int x, int y, int width, int height, ConfigEntry.Boolean entry) {
        super(x, y, width, height, entry.getTranslatedEntry());
        this.entry = entry;
    }

    @Override
    public void reset() {
        this.entry.reset();
        setMessage(this.entry.getTranslatedEntry());
        if (this.onChange != null)
            this.onChange.run();
    }

    @Override
    public boolean isNotDefault() {
        return this.entry.isNotDefault();
    }

    @Override
    public void onChange(Runnable action) {
        this.onChange = action;
    }

    @Override
    public Component onHover() {
        return this.entry.getTranslatedDescription();
    }

    @Override
    public void onPress() {
        this.entry.set(!this.entry.get());
        setMessage(this.entry.getTranslatedEntry());
        if (this.onChange != null)
            this.onChange.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            Component text = this.entry.getTranslatedEntry(!this.entry.get());
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", text));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", text));
            }
        }
    }
}
