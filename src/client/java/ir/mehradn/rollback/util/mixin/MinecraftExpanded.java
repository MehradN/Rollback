package ir.mehradn.rollback.util.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MinecraftExpanded {
    void runOnNextTick(Runnable runnable);
}
