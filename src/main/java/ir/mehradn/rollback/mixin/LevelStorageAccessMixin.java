package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class LevelStorageAccessMixin implements AutoCloseable, LevelStorageAccessExpanded {
    private Path latestBackupPath;

    public Path getLatestBackupPath() {
        return this.latestBackupPath;
    }

    @ModifyArg(method = "makeWorldBackup", at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/nio/file/Files;size(Ljava/nio/file/Path;)J"))
    private Path grabBackupPath(Path path) {
        this.latestBackupPath = path;
        return path;
    }
}
