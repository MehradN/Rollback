package ir.mehradn.rollback.event;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class AutomatedBackup {
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
            Rollback.LOGGER.info("Creating an automated backup...");
            BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
            backupManager.createRollbackBackup(server);
            latestBackup = server.getTicks();
        }
    }

    private static boolean shouldCreateBackup(MinecraftServer server) {
        int serverTick = server.getTicks();
        int worldTick = (int)server.getWorld(World.OVERWORLD).getTimeOfDay();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        String worldName = ((MinecraftServerExpanded)server).getSession().getLevelSummary().getName();

        if (!backupManager.getAutomated(worldName))
            return false;
        if (RollbackConfig.backupMode() == RollbackConfig.BackupMode.REAL_TIME)
            return (serverTick - latestBackup) >= (RollbackConfig.ticksPerBackup() + 20);
        else
            return (
                ((serverTick - latestBackup) >= (RollbackConfig.ticksPerBackup() / 2 - 20))
                    && (((worldTick - 20) % RollbackConfig.ticksPerBackup()) == 0)
            );
    }
}
