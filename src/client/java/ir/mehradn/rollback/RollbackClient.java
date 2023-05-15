package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackClientConfig;
import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.event.GuiCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RollbackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Rollback.LOGGER.info("Registering client events...");
        GuiCommand.register();
        Rollback.LOGGER.info("Loading config...");
        RollbackDefaultConfig.defaultSupplier = RollbackClientConfig::load;
    }
}