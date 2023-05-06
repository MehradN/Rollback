package ir.mehradn.rollback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.config.RollbackClientConfig;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.config.RollbackServerConfig;
import ir.mehradn.rollback.event.RollbackCommand;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import ir.mehradn.rollback.util.gson.PathAdapter;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Rollback implements ModInitializer {
    public static final String MOD_ID = "rollback";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(RollbackVersion.class, new RollbackVersion.Adapter())
        .registerTypeAdapter(RollbackServerConfig.class, new RollbackConfig.Adapter<>(RollbackServerConfig.class))
        .registerTypeAdapter(RollbackClientConfig.class, new RollbackConfig.Adapter<>(RollbackServerConfig.class))
        .registerTypeAdapter(RollbackWorldConfig.class, new RollbackConfig.Adapter<>(RollbackWorldConfig.class))
        .create();

    public void onInitialize() {
        LOGGER.info("Registering events...");
        RollbackCommand.register();
    }
}