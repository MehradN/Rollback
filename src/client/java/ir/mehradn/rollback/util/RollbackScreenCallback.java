package ir.mehradn.rollback.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface RollbackScreenCallback {
    void send(Action action, Screen lastScreen);

    @Environment(EnvType.CLIENT)
    enum Action {
        NOTHING,
        RELOAD,
        PLAY,
        RECREATE,
        ROLLBACK
    }
}
