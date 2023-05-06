package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackClientConfig;
import ir.mehradn.rollback.config.RollbackConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RollbackClient implements ClientModInitializer {
    public void onInitializeClient() {
        Rollback.LOGGER.info("Loading config...");
        RollbackConfig.DEFAULT = RollbackClientConfig.load();
    }
}