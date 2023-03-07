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
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

@Environment(EnvType.CLIENT)
public final class AutomatedBackup {
    private static int latestUpdate;
    private static int daysPassed;
    private static int sinceDay;
    private static int sinceBackup;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(AutomatedBackup::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(AutomatedBackup::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(AutomatedBackup::onServerStopping);
    }

    public static void onServerStarted(MinecraftServer server) {
        Rollback.LOGGER.info("Reading the timer information...");
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();
        Triple<Integer, Integer, Integer> info = backupManager.getTimerInformation(worldName);

        latestUpdate = server.getTickCount();
        daysPassed = info.getLeft();
        sinceDay = info.getMiddle();
        sinceBackup = info.getRight();
    }

    public static void onEndTick(MinecraftServer server) {
        int serverTick = server.getTickCount();
        int worldTick = (int)server.getLevel(Level.OVERWORLD).getDayTime();

        if (shouldUpdate(serverTick, worldTick)) {
            Rollback.LOGGER.info("Updating the timer information...");
            BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
            String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();

            int timePassed = serverTick - latestUpdate;
            latestUpdate = serverTick;
            sinceDay += timePassed;
            sinceBackup += timePassed;
            if (isMorning(worldTick) && sinceDay >= 11900) {
                daysPassed++;
                sinceDay = 0;
            }

            if (shouldCreateBackup(worldTick, backupManager, worldName)) {
                Rollback.LOGGER.info("Creating an automated backup...");
                backupManager.createRollbackBackup(server, true);
                daysPassed = 0;
                sinceDay = 0;
                sinceBackup = 0;
            } else {
                backupManager.setTimerInformation(worldName, daysPassed, sinceBackup);
            }
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        int serverTick = server.getTickCount();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();

        int timePassed = serverTick - latestUpdate;
        sinceDay += timePassed;
        sinceBackup += timePassed;

        backupManager.setTimerInformation(worldName, daysPassed, sinceDay, sinceBackup);
    }

    private static boolean isMorning(int worldTick) {
        return ((worldTick - 20) % 24000 == 0);
    }

    private static boolean shouldUpdate(int serverTick, int worldTick) {
        return (isMorning(worldTick) || (serverTick - latestUpdate + sinceBackup) % 24000 == 0);
    }

    private static boolean shouldCreateBackup(int worldTick, BackupManager backupManager, String worldName) {
        if (backupManager.getAutomated(worldName)) {
            switch (RollbackConfig.timerMode()) {
                case DAYLIGHT_CYCLE -> {return (isMorning(worldTick) && daysPassed >= RollbackConfig.daysPerBackup());}
                case IN_GAME_TIME -> {return (sinceBackup >= RollbackConfig.ticksPerBackup());}
            }
        }
        return false;
    }
}
