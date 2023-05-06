package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.config.RollbackServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public class RollbackServer implements DedicatedServerModInitializer {
    public void onInitializeServer() {
        Rollback.LOGGER.info("Loading config...");
        RollbackConfig.DEFAULT = RollbackServerConfig.load();
    }
}
