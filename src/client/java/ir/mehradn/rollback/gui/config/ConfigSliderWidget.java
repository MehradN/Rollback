package ir.mehradn.rollback.gui.config;

import ir.mehradn.rollback.config.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ConfigSliderWidget extends AbstractSliderButton implements ConfigWidget {
    private final ConfigEntry.Short entry;
    private Runnable onChange;

    public ConfigSliderWidget(int x, int y, int width, int height, ConfigEntry.Short entry) {
        super(x, y, width, height, entry.getTranslatedEntry(), decompileValue(entry.min, entry.max, entry.get()));
        this.entry = entry;
    }

    @Override
    public void reset() {
        this.entry.reset();
        this.value = decompileValue(this.entry.min, this.entry.max, this.entry.get());
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
    protected void updateMessage() {
        setMessage(this.entry.getTranslatedEntry());
    }

    @Override
    protected void applyValue() {
        this.entry.set(compileValue(this.entry.min, this.entry.max, this.value));
        if (this.onChange != null)
            this.onChange.run();
    }

    private static short compileValue(short min, short max, double value) {
        return (short)Math.round(min + value * (max - min));
    }

    private static double decompileValue(short min, short max, short value) {
        return (double)(value - min) / (max - min);
    }
}
