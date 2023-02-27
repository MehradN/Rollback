package ir.mehradn.rollback.event;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class AutomatedBackup {
    private static int latestBackup;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(AutomatedBackup::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(AutomatedBackup::onEndTick);
    }

    public static void onServerStarted(MinecraftServer server) {
        latestBackup = server.getTicks();
    }

    public static void onEndTick(MinecraftServer server) {
        if (shouldCreateBackup(server)) {
            BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
            backupManager.createRollbackBackup(server);
            latestBackup = server.getTicks();
        }
    }

    private static boolean shouldCreateBackup(MinecraftServer server) {
        int serverTick = server.getTicks();
        int worldTick = (int)server.getWorld(World.OVERWORLD).getTimeOfDay();
        GameMode gameMode = ((MinecraftServerExpanded)server).getSession().getLevelSummary().getGameMode();

        if (!RollbackConfig.isAllowedWorldType(gameMode))
            return false;
        if (RollbackConfig.getBackupMode() == RollbackConfig.BackupMode.REAL_TIME)
            return (serverTick - latestBackup) >= (RollbackConfig.getBackupTicks() + 20);
        return ((serverTick - latestBackup) >= (RollbackConfig.getBackupTicks() / 2 - 20)) && (((worldTick - 20) % RollbackConfig.getBackupTicks()) == 0);
    }
}
