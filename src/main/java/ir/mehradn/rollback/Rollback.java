package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.event.AutomatedBackup;
import ir.mehradn.rollback.event.RollbackCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class Rollback implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("rollback");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Registering MidnightConfig...");
        RollbackConfig.register();
        LOGGER.info("Registering events...");
        AutomatedBackup.register();
        RollbackCommand.register();
    }
}