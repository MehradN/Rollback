package ir.mehradn.rollback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.Rollback;
import net.fabricmc.loader.api.FabricLoader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class RollbackDefaultConfig extends RollbackConfig {
    public static Supplier<RollbackDefaultConfig> defaultSupplier;
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RollbackDefaultConfig.class, new Adapter<>(RollbackDefaultConfig.class))
        .setPrettyPrinting().create();
    // ConfigEntry.Boolean("backupEnabled", false)
    // ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5)
    // ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1)
    // ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)

    public RollbackDefaultConfig() {
        super(
            new ConfigEntry.Boolean("backupEnabled", false),
            new ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5),
            new ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1),
            new ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
        );
    }

    public static RollbackDefaultConfig load() {
        return load(GSON, RollbackDefaultConfig.class, RollbackDefaultConfig::new);
    }

    public void save() throws IOException {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(Rollback.MOD_ID + ".json");
        try {
            Files.createDirectories(configDir);
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                getGson().toJson(this, writer);
            }
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the config file!", e);
            throw e;
        }
    }

    protected static <T extends RollbackDefaultConfig> T load(Gson gson, Class<T> type, Supplier<T> constructor) {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(Rollback.MOD_ID + ".json");
        try (FileReader reader = new FileReader(configFile.toFile())) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            Rollback.LOGGER.warn("Failed to load the config file!", e);
            return constructor.get();
        }
    }

    protected Gson getGson() {
        return GSON;
    }
}
