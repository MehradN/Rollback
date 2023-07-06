package ir.mehradn.rollback;

import ir.mehradn.mehradconfig.entrypoint.ModMenuConfig;
import ir.mehradn.rollback.event.AutomatedBackup;
import ir.mehradn.rollback.event.RollbackCommand;
import ir.mehradn.rollback.event.ServerPacketListener;
import ir.mehradn.rollback.event.ServerTickTimer;
import ir.mehradn.rollback.util.GlobalSuppliers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rollback implements ModInitializer {
    public static final String MOD_ID = "rollback";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Registering events...");
        RollbackCommand.register();
        AutomatedBackup.register();
        ServerTickTimer.register();
        ServerPacketListener.register();
        ModMenuConfig.register(MOD_ID, GlobalSuppliers::buildDefaultConfig);
    }
}