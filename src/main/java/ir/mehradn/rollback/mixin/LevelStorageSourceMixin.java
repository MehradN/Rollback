package ir.mehradn.rollback.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {
    @ModifyArg(method = "createDefault", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource;<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;)V"))
    private static Path changeBackupDir(Path baseDir, Path backupDir, DataFixer dataFixer) {
        if (backupDir.equals(baseDir.resolve("../backups")))
            return baseDir.resolve("backups");
        return backupDir;
    }
}
