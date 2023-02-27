package ir.mehradn.rollback;

import eu.midnightdust.lib.config.MidnightConfig;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.event.AutomatedBackup;
import ir.mehradn.rollback.event.RollbackCommand;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rollback implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("rollback");

    @Override
    public void onInitializeClient() {
        MidnightConfig.init("rollback", RollbackConfig.class);
        AutomatedBackup.register();
        RollbackCommand.register();
    }
}