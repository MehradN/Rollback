package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements CommandSource, AutoCloseable, MinecraftServerExpanded {
    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    private BackupManager backupManager;

    public MinecraftServerMixin(String string) {
        super(string);
    }

    public LevelStorageSource.LevelStorageAccess getLevelAccess() {
        return this.storageSource;
    }

    public BackupManager getBackupManager() {
        return this.backupManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBackupManager(CallbackInfo ci) {
        this.backupManager = new BackupManager();
    }
}
