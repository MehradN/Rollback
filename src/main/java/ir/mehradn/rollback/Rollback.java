package ir.mehradn.rollback;

import ir.mehradn.rollback.event.RollbackCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rollback implements ModInitializer {
    public static final String MOD_ID = "rollback";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        LOGGER.info("Registering events...");
        RollbackCommand.register();
    }
}