package ir.mehradn.rollback.gui.config;

import net.minecraft.network.chat.Component;

public interface ConfigWidget {
    void reset();

    boolean isNotDefault();

    void onChange(Runnable action);

    Component onHover();
}
