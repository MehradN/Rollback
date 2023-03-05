package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements CommandOutput, AutoCloseable, MinecraftServerExpanded {
    @Shadow @Final protected LevelStorage.Session session;

    private BackupManager backupManager;

    public MinecraftServerMixin(String string) {
        super(string);
    }

    public LevelStorage.Session getSession() {
        return this.session;
    }

    public BackupManager getBackupManager() {
        return this.backupManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBackupManager(CallbackInfo ci) {
        this.backupManager = new BackupManager();
    }
}
