package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.LevelStorageSessionExpanded;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.nio.file.Path;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin implements AutoCloseable, LevelStorageSessionExpanded {
    private Path latestBackupPath;

    @ModifyArg(method = "createBackup", at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/nio/file/Files;size(Ljava/nio/file/Path;)J"))
    private Path grabBackupPath(Path path) {
        this.latestBackupPath = path;
        return path;
    }

    public Path getLatestBackupPath() {
        return this.latestBackupPath;
    }
}
