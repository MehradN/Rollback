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
        .create();
    // ConfigEntry<>("backupEnabled", Boolean.class, false, null),
    // ConfigEntry<>("maxBackups", Short.class, (short)5, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Short.class, (short)1, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)

    public RollbackDefaultConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, false, null),
            new ConfigEntry<>("maxBackups", Short.class, (short)5, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Short.class, (short)1, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)
        );
    }

    public static RollbackDefaultConfig load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(Rollback.MOD_ID + ".json");
        try (FileReader reader = new FileReader(configFile.toFile())) {
            return GSON.fromJson(reader, RollbackDefaultConfig.class);
        } catch (IOException e) {
            Rollback.LOGGER.warn("Failed to load the config file!", e);
            return new RollbackDefaultConfig();
        }
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

    protected Gson getGson() {
        return GSON;
    }
}
