package ir.mehradn.util.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BackupManager {
    private JsonObject backupInfo;
    private final Path backupDirectory;
    private final Path backupInfoFile;

    public BackupManager() {
        backupDirectory = MinecraftClient.getInstance().getLevelStorage().getBackupsDirectory();
        backupInfoFile = Path.of(backupDirectory.toString(), "rollbacks", "rollbacks.json");
        try {
            backupInfo = JsonParser.parseReader(new FileReader(backupInfoFile.toFile())).getAsJsonObject();
        } catch (FileNotFoundException e) {
            backupInfo = new JsonObject();
            saveBackupInfo();
        }
    }

    private void saveBackupInfo() {
        try {
            Files.createDirectories(Path.of(backupDirectory.toString(), "rollbacks"));
            FileWriter writer = new FileWriter(backupInfoFile.toFile());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(backupInfo, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNormalBackup(LevelSummary summary) {
        long l;
        try (LevelStorage.Session session = MinecraftClient.getInstance().getLevelStorage().createSession(summary.getName())) {
            l = session.createBackup();
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("selectWorld.edit.backupCreated", session.getDirectoryName()),
                    Text.translatable("selectWorld.edit.backupSize", MathHelper.ceil((double)l / 1048576.0))
            ));
        } catch (IOException e) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("selectWorld.edit.backupFailed"),
                    Text.literal(e.getMessage())
            ));
        }
    }

    /*public void createRollbackBackup() {}

    public void loadBackupInfo() {}

    public List<RollbackBackup> getRollbacksFor(String worldName) {}

    public void rollbackTo(RollbackBackup backup) {}*/
}
