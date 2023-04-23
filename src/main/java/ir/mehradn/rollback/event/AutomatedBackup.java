package ir.mehradn.rollback.event;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public final class AutomatedBackup {
    private static BackupManager backupManager;
    private static RollbackWorld rollbackWorld;
    private static int latestUpdate;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(AutomatedBackup::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(AutomatedBackup::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(AutomatedBackup::onServerStopping);
    }

    public static void onServerStarted(MinecraftServer server) {
        Rollback.LOGGER.info("Reading the timer information...");
        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();
        backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        rollbackWorld = backupManager.getWorld(worldName);
        latestUpdate = server.getTickCount();
    }

    public static void onEndTick(MinecraftServer server) {
        int serverTick = server.getTickCount();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        int worldTick = (overworld == null ? -1 : (int)overworld.getDayTime());

        if (shouldUpdate(serverTick, worldTick)) {
            Rollback.LOGGER.info("Updating the timer information...");
            int timePassed = serverTick - latestUpdate;
            latestUpdate = serverTick;
            rollbackWorld.ticksSinceLastMorning += timePassed;
            rollbackWorld.ticksSinceLastBackup += timePassed;
            if (isMorning(worldTick) && rollbackWorld.ticksSinceLastMorning >= 11900) {
                rollbackWorld.daysSinceLastBackup++;
                rollbackWorld.ticksSinceLastMorning = 0;
            }

            if (shouldCreateBackup(worldTick)) {
                Rollback.LOGGER.info("Creating an automated backup...");
                backupManager.createRollbackBackup(server, null);
                rollbackWorld.resetTimers();
            }

            backupManager.saveMetadata();
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        int serverTick = server.getTickCount();
        int timePassed = serverTick - latestUpdate;
        rollbackWorld.ticksSinceLastMorning += timePassed;
        rollbackWorld.ticksSinceLastBackup += timePassed;
        backupManager.saveMetadata();
    }

    private static boolean isMorning(int worldTick) {
        if (worldTick == -1)
            return false;
        return ((worldTick - 20) % 24000 == 0);
    }

    private static boolean shouldUpdate(int serverTick, int worldTick) {
        return (isMorning(worldTick) || (serverTick - latestUpdate + rollbackWorld.ticksSinceLastBackup) % 24000 == 0);
    }

    private static boolean shouldCreateBackup(int worldTick) {
        if (rollbackWorld.automatedBackups) {
            switch (RollbackConfig.timerMode) {
                case DAYLIGHT_CYCLE -> { return (isMorning(worldTick) && rollbackWorld.daysSinceLastBackup >= RollbackConfig.daysPerBackup()); }
                case IN_GAME_TIME -> { return (rollbackWorld.ticksSinceLastBackup >= RollbackConfig.ticksPerBackup()); }
            }
        }
        return false;
    }
}
