package ir.mehradn.rollback.util.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelSummary;

@Environment(EnvType.CLIENT)
public class PublicStatics {
    public static LevelSummary playWorld = null;
    public static LevelSummary recreateWorld = null;
    public static LevelSummary rollbackWorld = null;
}
