package ir.mehradn.rollback.util.backup;

import com.google.gson.*;
import ir.mehradn.rollback.mixin.GameRendererAccessor;
import ir.mehradn.rollback.util.mixin.LevelStorageSessionExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BackupManager {
    private JsonObject backupInfo;
    public final Path rollbackDirectory;
    public final Path iconsDirectory;
    private final Path backupInfoFile;

    public BackupManager() {
        rollbackDirectory = Path.of(MinecraftClient.getInstance().getLevelStorage().getBackupsDirectory().toString(), "rollbacks");
        iconsDirectory = Path.of(rollbackDirectory.toString(), "icons");
        backupInfoFile = Path.of(rollbackDirectory.toString(), "rollbacks.json");
        try {
            this.loadBackupInfo();
        } catch (FileNotFoundException e) {
            backupInfo = new JsonObject();
            try {
                saveBackupInfo();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void loadBackupInfo() throws FileNotFoundException {
        backupInfo = JsonParser.parseReader(new FileReader(backupInfoFile.toFile())).getAsJsonObject();
    }

    private void saveBackupInfo() throws IOException {
        Files.createDirectories(rollbackDirectory);
        Files.createDirectories(iconsDirectory);
        FileWriter writer = new FileWriter(backupInfoFile.toFile());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(backupInfo, writer);
        writer.close();
    }

    private void showErrorToast(IOException e) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                Text.translatable("selectWorld.edit.backupFailed"),
                Text.literal(e.getMessage())
        ));
    }

    private int getMaxBackupCount() {
        return 5;
    }

    public boolean createNormalBackup(LevelSummary summary) {
        long l;
        try (LevelStorage.Session session = MinecraftClient.getInstance().getLevelStorage().createSession(summary.getName())) {
            l = session.createBackup();
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("selectWorld.edit.backupCreated", session.getDirectoryName()),
                    Text.translatable("selectWorld.edit.backupSize", MathHelper.ceil((double)l / 1048576.0))
            ));
            return true;
        } catch (IOException e) {
            showErrorToast(e);
            return false;
        }
    }

    public boolean createRollbackBackup(MinecraftServer server) {
        LevelStorage.Session session = ((MinecraftServerExpanded) server).getSession();
        String worldName = session.getLevelSummary().getName();
        if (!this.backupInfo.has(worldName))
            this.backupInfo.add(worldName, new JsonArray());
        JsonArray array = this.backupInfo.getAsJsonArray(worldName);

        while (array.size() >= getMaxBackupCount())
            deleteOldestBackup(worldName);

        boolean bl = server.saveAll(true, false, true);
        if (!bl) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("selectWorld.edit.backupFailed"),
                    Text.translatable("commands.save.failed")
            ));
            return false;
        }

        try {
            session.createBackup();
        } catch (IOException e) {
            showErrorToast(e);
            return false;
        }

        Path path1 = ((LevelStorageSessionExpanded) session).getLatestBackupPath();
        Path path2 = Path.of(rollbackDirectory.toString(), path1.getFileName().toString());
        try {
            Files.move(path1, path2);
        } catch (IOException e) {
            showErrorToast(e);
            return false;
        }

        Path path3;
        try {
            path3 = iconsDirectory.resolve(PathUtil.getNextUniqueName(iconsDirectory, session.getDirectoryName(), ".png"));
            Path finalPath = path3;
            MinecraftClient.getInstance().execute(() -> {
                GameRenderer renderer =  MinecraftClient.getInstance().gameRenderer;
                ((GameRendererAccessor) renderer).InvokeUpdateWorldIcon(finalPath);
            });
        } catch (IOException e) {
            showErrorToast(e);
            return false;
        }

        int daysPlayed = (int) (MinecraftClient.getInstance().world.getTimeOfDay() / 24000);

        path2 = rollbackDirectory.relativize(path2);
        path3 = rollbackDirectory.relativize(path3);
        RollbackBackup backup = new RollbackBackup(worldName, path2, path3, LocalDateTime.now(), daysPlayed);
        array.add(backup.toObject());

        try {
            saveBackupInfo();
        } catch (IOException e) {
            showErrorToast(e);
            return false;
        }

        return true;
    }

    public List<RollbackBackup> getRollbacksFor(String worldName) {
        ArrayList<RollbackBackup> list = new ArrayList<>();
        if (!backupInfo.has(worldName))
            return list;

        JsonArray array = backupInfo.getAsJsonArray(worldName);
        for (JsonElement elm : array)
            list.add(new RollbackBackup(worldName, elm.getAsJsonObject()));
        return list;
    }

    public void deleteOldestBackup(String worldName) {
        if (!backupInfo.has(worldName))
            return;

        JsonArray array = backupInfo.getAsJsonArray(worldName);
        RollbackBackup backup = new RollbackBackup(worldName, array.get(0).getAsJsonObject());
        array.remove(0);

        try {
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.iconPath.toString()));
        } catch (IOException ignored) {}
        try {
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.backupPath.toString()));
        } catch (IOException ignored) {}

        try {
            saveBackupInfo();
        } catch (IOException ignored) {}
    }

    public void deleteLatestBackup(String worldName) {
        if (!backupInfo.has(worldName))
            return;

        JsonArray array = backupInfo.getAsJsonArray(worldName);
        int index = array.size() - 1;
        RollbackBackup backup = new RollbackBackup(worldName, array.get(index).getAsJsonObject());
        array.remove(index);

        try {
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.iconPath.toString()));
        } catch (IOException ignored) {}
        try {
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.backupPath.toString()));
        } catch (IOException ignored) {}

        try {
            saveBackupInfo();
        } catch (IOException ignored) {}
    }

    /*public void rollbackTo(RollbackBackup backup) {}*/
}
