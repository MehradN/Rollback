package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.util.GlobalSuppliers;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.io.IOException;

@Environment(EnvType.SERVER)
public class RollbackServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Rollback.LOGGER.info("Setting static suppliers...");
        GlobalSuppliers.setDefaultConfigSupplier(RollbackServer::loadConfig);
    }

    private static RollbackDefaultConfig loadConfig() {
        RollbackDefaultConfig defaultConfig = new RollbackDefaultConfig();
        try {
            defaultConfig.load();
            return defaultConfig;
        } catch (IOException e) {
            return new RollbackDefaultConfig();
        }
    }
}
