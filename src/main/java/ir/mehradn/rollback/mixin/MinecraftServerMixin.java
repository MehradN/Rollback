package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.rollback.ChatEventAnnouncer;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.ServerGofer;
import ir.mehradn.rollback.rollback.exception.BackupIOException;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
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

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask>
    implements CommandSource, AutoCloseable, MinecraftServerExpanded {
    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;
    private CommonBackupManager backupManager;

    public MinecraftServerMixin(String string) {
        super(string);
    }

    public LevelStorageSource.LevelStorageAccess getLevelStorageAccess() {
        return this.storageSource;
    }

    public LevelStorageSource getLevelStorageSource() {
        return ((LevelStorageAccessExpanded)this.storageSource).getSource();
    }

    public CommonBackupManager getBackupManager() {
        return this.backupManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBackupManager(CallbackInfo ci) {
        ServerGofer gofer = new ServerGofer((MinecraftServer)(Object)this);
        this.backupManager = new CommonBackupManager(gofer);
        this.backupManager.eventAnnouncer = new ChatEventAnnouncer(this);

        try {
            this.backupManager.loadData();
        } catch (BackupIOException e) {
            throw new RuntimeException(e);
        }
    }
}
