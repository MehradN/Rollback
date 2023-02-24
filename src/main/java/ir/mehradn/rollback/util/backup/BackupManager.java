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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    private void showErrorToast(String title, IOException e) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                Text.translatable(title),
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
            showErrorToast("selectWorld.edit.backupFailed", e);
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
            deleteBackup(worldName, 0);

        boolean bl = server.saveAll(true, false, true);
        if (!bl) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                    Text.translatable("rollback.createBackup.failed"),
                    Text.translatable("commands.save.failed")
            ));
            return false;
        }

        try {
            session.createBackup();
        } catch (IOException e) {
            showErrorToast("rollback.createBackup.failed", e);
            return false;
        }

        Path path1 = ((LevelStorageSessionExpanded) session).getLatestBackupPath();
        Path path2 = Path.of(rollbackDirectory.toString(), path1.getFileName().toString());
        try {
            Files.move(path1, path2);
        } catch (IOException e) {
            showErrorToast("rollback.createBackup.failed", e);
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
            showErrorToast("rollback.createBackup.failed", e);
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
            showErrorToast("rollback.createBackup.failed", e);
            return false;
        }

        return true;
    }

    public boolean deleteBackup(String worldName, int index) {
        if (!backupInfo.has(worldName))
            return true;

        JsonArray array = backupInfo.getAsJsonArray(worldName);
        if (index == -1)
            index += array.size();
        if (array.size() <= index)
            return true;

        RollbackBackup backup = new RollbackBackup(worldName, array.get(index).getAsJsonObject());

        try {
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.iconPath.toString()));
            Files.deleteIfExists(Path.of(rollbackDirectory.toString(), backup.backupPath.toString()));
        } catch (IOException e) {
            showErrorToast("rollback.deleteBackup.failed", e);
            return false;
        }

        JsonArray oldArray = array.deepCopy();
        array.remove(index);
        try {
            saveBackupInfo();
        } catch (IOException e) {
            backupInfo.add(worldName, oldArray);
            showErrorToast("rollback.deleteBackup.failed", e);
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

    public boolean rollbackTo(RollbackBackup backup) {
        MinecraftClient client = MinecraftClient.getInstance();
        try (LevelStorage.Session session = client.getLevelStorage().createSession(backup.worldName)) {
            session.deleteSessionLock();
        } catch (IOException e) {
            showErrorToast("rollback.rollback.failed", e);
            return false;
        }

        Path source = Path.of(rollbackDirectory.toString(), backup.backupPath.toString());
        Path dest = client.getLevelStorage().getSavesDirectory();
        try (ZipFile zip = new ZipFile(source.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = Path.of(dest.toString(), entry.getName());
                if (entry.isDirectory())
                    Files.createDirectories(path);
                else {
                    Files.createDirectories(path.getParent());
                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(path)) {
                        in.transferTo(out);
                    }
                }
            }
        } catch (IOException e) {
            showErrorToast("rollback.rollback.failed", e);
            return false;
        }

        return true;
    }
}
