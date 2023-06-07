package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.util.GlobalSuppliers;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public class RollbackServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Rollback.LOGGER.info("Setting static suppliers...");
        GlobalSuppliers.setDefaultConfigSupplier(RollbackDefaultConfig::load);
    }
}
