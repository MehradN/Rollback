package ir.mehradn.rollback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.Rollback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class RollbackClientConfig extends RollbackDefaultConfig {
    public final ConfigEntry<Boolean> replaceButton = new ConfigEntry<>("replaceButton", Boolean.class, true, null);
    public final ConfigEntry<Boolean> promptEnabled = new ConfigEntry<>("promptEnabled", Boolean.class, true, null);
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RollbackClientConfig.class, new RollbackConfig.Adapter<>(RollbackClientConfig.class))
        .create();

    public RollbackClientConfig() {
        super();
        this.entries.add(this.replaceButton);
        this.entries.add(this.promptEnabled);
    }

    public static RollbackClientConfig load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(Rollback.MOD_ID + ".json");
        try (FileReader reader = new FileReader(configFile.toFile())) {
            return GSON.fromJson(reader, RollbackClientConfig.class);
        } catch (IOException e) {
            Rollback.LOGGER.warn("Failed to load the config file!", e);
            return new RollbackClientConfig();
        }
    }

    @Override
    public Gson getGson() {
        return GSON;
    }
}
