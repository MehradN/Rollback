package ir.mehradn.rollback.util.mixin;

import net.minecraft.world.level.storage.LevelSummary;

public class PublicStatics {
    public static LevelSummary playWorld = null;
    public static LevelSummary recreateWorld = null;
    public static LevelSummary rollbackWorld = null;
}
