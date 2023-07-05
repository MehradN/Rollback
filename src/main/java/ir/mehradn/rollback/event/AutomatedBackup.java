package ir.mehradn.rollback.event;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.ServerBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public final class AutomatedBackup {
    private static final int DAY = 24000;
    private static ServerBackupManager backupManager;
    private static long latestUpdate;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(AutomatedBackup::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(AutomatedBackup::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(AutomatedBackup::onServerStopping);
    }

    private static void onServerStarted(MinecraftServer server) {
        Rollback.LOGGER.info("Reading the timer information...");
        backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        latestUpdate = server.getTickCount();
    }

    private static void onEndTick(MinecraftServer server) {
        RollbackWorld world = backupManager.getWorld();
        long serverTick = server.getTickCount();
        long worldTick = server.overworld().getDayTime();

        if (isMorning(worldTick) || twentyMinPassed(serverTick)) {
            Rollback.LOGGER.info("Updating the timer information...");
            long passed = serverTick - latestUpdate;
            latestUpdate = serverTick;
            world.ticksSinceLastMorning += passed;
            world.ticksSinceLastBackup += passed;
            if (isMorning(worldTick) && world.ticksSinceLastMorning >= DAY / 2 - 100) {
                world.daysSinceLastBackup++;
                world.ticksSinceLastMorning = 0;
            }

            if (shouldCreateBackup(worldTick)) {
                Rollback.LOGGER.info("Creating an automated backup...");
                try {
                    backupManager.createBackup(BackupType.ROLLBACK, null);
                    world.ticksSinceLastMorning = 0;
                    world.ticksSinceLastBackup = 0;
                    world.daysSinceLastBackup = 0;
                } catch (BackupManagerException e) {
                    Rollback.LOGGER.error("Failed to create an automated backup!", e);
                }
            }

            try {
                backupManager.saveWorld();
            } catch (BackupManagerException e) {
                Rollback.LOGGER.error("Failed to save the timer information!", e);
            }
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        RollbackWorld world = backupManager.getWorld();
        long serverTick = server.getTickCount();
        long passed = serverTick - latestUpdate;
        world.ticksSinceLastMorning += passed;
        world.ticksSinceLastBackup += passed;

        try {
            backupManager.saveWorld();
        } catch (BackupManagerException e) {
            Rollback.LOGGER.error("Failed to save the timer information!", e);
        }
    }

    private static boolean isMorning(long worldTick) {
        return (worldTick - 20) % DAY == 0;
    }

    private static boolean twentyMinPassed(long serverTick) {
        return (serverTick - latestUpdate + backupManager.getWorld().ticksSinceLastBackup) % DAY == 0;
    }

    private static boolean shouldCreateBackup(long worldTick) {
        RollbackWorld world = backupManager.getWorld();
        if (world.config.backupEnabled.get()) {
            return switch (world.config.timerMode.get()) {
                case DAYLIGHT_CYCLE -> isMorning(worldTick) && world.daysSinceLastBackup >= world.config.backupFrequency.get();
                case IN_GAME_TIME -> world.ticksSinceLastBackup >= world.config.backupFrequency.get() * DAY;
            };
        }
        return false;
    }
}
