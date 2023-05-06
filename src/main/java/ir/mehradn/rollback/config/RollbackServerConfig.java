package ir.mehradn.rollback.config;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.loader.api.FabricLoader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RollbackServerConfig extends RollbackConfig {
    // backupEnabled = new ConfigEntry<>("backupEnabled", Boolean.class, false, null, null);
    // maxBackups = new ConfigEntry<>("maxBackups", Integer.class, 5, null, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED));
    // backupFrequency = new ConfigEntry<>("backupFrequency", Integer.class, 1, null, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY));
    // timerMode = new ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null, null);

    public RollbackServerConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, false, null, null),
            new ConfigEntry<>("maxBackups", Integer.class, 5, null, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Integer.class, 1, null, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null, null)
        );
    }

    public static RollbackServerConfig load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve(Rollback.MOD_ID + ".json");
        try (FileReader reader = new FileReader(configFile.toFile())) {
            return Rollback.GSON.fromJson(reader, RollbackServerConfig.class);
        } catch (IOException e) {
            Rollback.LOGGER.warn("Failed to load the config file!", e);
            return new RollbackServerConfig();
        }
    }

    public void save() throws IOException {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(Rollback.MOD_ID + ".json");
        try {
            Files.createDirectories(configDir);
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                Rollback.GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to save the config file!", e);
            throw e;
        }
    }
}
