package ir.mehradn.rollback.util.mixin;

import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;

public class PublicStatics {
    @Nullable public static LevelSummary recreateWorld = null;
    @Nullable public static LevelSummary rollbackWorld = null;
}
