package ir.mehradn.rollback;

import ir.mehradn.rollback.config.RollbackClientConfig;
import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.event.ClientPacketListener;
import ir.mehradn.rollback.util.GlobalSuppliers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class RollbackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Rollback.LOGGER.info("Setting global suppliers...");
        GlobalSuppliers.setDefaultConfigSupplier(RollbackClient::loadConfig);
        Rollback.LOGGER.info("Registering client events...");
        ClientPacketListener.register();
    }

    private static RollbackDefaultConfig loadConfig() {
        RollbackClientConfig config = new RollbackClientConfig();
        try {
            config.load();
            return config;
        } catch (IOException e) {
            return new RollbackDefaultConfig();
        }
    }
}