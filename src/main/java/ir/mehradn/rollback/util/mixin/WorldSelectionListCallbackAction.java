package ir.mehradn.rollback.util.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum WorldSelectionListCallbackAction {
    NOTHING,
    RELOAD_WORLD_LIST,
    JOIN_WORLD,
    RECREATE_WORLD,
    ROLLBACK_WORLD
}
