package ir.mehradn.rollback.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class LevelStorageAccessMixin implements AutoCloseable, LevelStorageAccessExpanded {
    private Path latestBackupPath;

    public Path getLatestBackupPath() {
        return this.latestBackupPath;
    }

    @WrapOperation(method = "makeWorldBackup", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;size(Ljava/nio/file/Path;)J"))
    private long grabBackupPath(Path path, Operation<Long> original) {
        this.latestBackupPath = path;
        return original.call(path);
    }
}
