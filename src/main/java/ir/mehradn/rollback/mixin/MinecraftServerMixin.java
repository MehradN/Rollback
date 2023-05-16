package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.ServerBackupManager;
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
    private ServerBackupManager backupManager;

    public MinecraftServerMixin(String string) {
        super(string);
    }

    @Override
    public LevelStorageSource.LevelStorageAccess getLevelStorageAccess() {
        return this.storageSource;
    }

    @Override
    public LevelStorageSource getLevelStorageSource() {
        return ((LevelStorageAccessExpanded)this.storageSource).getSource();
    }

    @Override
    public ServerBackupManager getBackupManager() {
        return this.backupManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBackupManager(CallbackInfo ci) {
        //noinspection DataFlowIssue
        this.backupManager = new ServerBackupManager((MinecraftServer)(Object)this);

        try {
            this.backupManager.loadWorld();
        } catch (BackupManagerException e) {
            throw new RuntimeException(e);
        }
    }
}
