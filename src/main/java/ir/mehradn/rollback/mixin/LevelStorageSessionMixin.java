package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.LevelStorageSessionExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin implements AutoCloseable, LevelStorageSessionExpanded {
    private Path latestBackupPath;

    public Path getLatestBackupPath() {
        return this.latestBackupPath;
    }

    @ModifyArg(method = "createBackup", at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/nio/file/Files;size(Ljava/nio/file/Path;)J"))
    private Path grabBackupPath(Path path) {
        this.latestBackupPath = path;
        return path;
    }
}
