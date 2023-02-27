package ir.mehradn.rollback.util.backup;

import com.google.gson.*;
import ir.mehradn.rollback.config.RollbackConfig;
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
        this.rollbackDirectory = MinecraftClient.getInstance().getLevelStorage().getBackupsDirectory().resolve("rollbacks");
        this.iconsDirectory = this.rollbackDirectory.resolve("icons");
        this.backupInfoFile = this.rollbackDirectory.resolve("rollbacks.json");
        try {
            loadBackupInfo();
        } catch (FileNotFoundException e) {
            this.backupInfo = new JsonObject();
            try {
                saveBackupInfo();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void loadBackupInfo() throws FileNotFoundException {
        this.backupInfo = JsonParser.parseReader(new FileReader(this.backupInfoFile.toFile())).getAsJsonObject();
    }

    private void saveBackupInfo() throws IOException {
        Files.createDirectories(this.rollbackDirectory);
        Files.createDirectories(this.iconsDirectory);
        FileWriter writer = new FileWriter(this.backupInfoFile.toFile());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(this.backupInfo, writer);
        writer.close();
    }

    private void showErrorToast(String title, IOException e) {
        MinecraftClient.getInstance().getToastManager().add(
            new SystemToast(SystemToast.Type.WORLD_BACKUP,
                Text.translatable(title),
                Text.literal(e.getMessage())
            ));
    }

    public boolean createNormalBackup(LevelSummary summary) {
        long l;
        try (LevelStorage.Session session = MinecraftClient.getInstance().getLevelStorage().createSession(summary.getName())) {
            l = session.createBackup();
            MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.WORLD_BACKUP,
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
        LevelStorage.Session session = ((MinecraftServerExpanded)server).getSession();
        String worldName = session.getLevelSummary().getName();
        LocalDateTime now = LocalDateTime.now();
        if (!this.backupInfo.has(worldName))
            this.backupInfo.add(worldName, new JsonArray());
        JsonArray array = this.backupInfo.getAsJsonArray(worldName);

        while (array.size() >= RollbackConfig.getMaxBackups())
            deleteBackup(worldName, 0);

        boolean bl = server.saveAll(true, false, true);
        if (!bl) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                SystemToast.Type.WORLD_BACKUP,
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

        Path path1 = ((LevelStorageSessionExpanded)session).getLatestBackupPath();
        Path path2;
        try {
            path2 = this.rollbackDirectory.resolve(PathUtil.getNextUniqueName(
                this.rollbackDirectory,
                now.format(RollbackBackup.TIME_FORMATTER) + "_" + worldName,
                ".zip"
            ));
            Files.move(path1, path2);
        } catch (IOException e) {
            showErrorToast("rollback.createBackup.failed", e);
            return false;
        }

        Path path3;
        try {
            path3 = this.iconsDirectory.resolve(PathUtil.getNextUniqueName(this.iconsDirectory, session.getDirectoryName(), ".png"));
            Path finalPath = path3;
            MinecraftClient.getInstance().execute(() -> {
                GameRenderer renderer = MinecraftClient.getInstance().gameRenderer;
                ((GameRendererAccessor)renderer).InvokeUpdateWorldIcon(finalPath);
            });
        } catch (IOException e) {
            showErrorToast("rollback.createBackup.failed", e);
            return false;
        }

        int daysPlayed = (int)(MinecraftClient.getInstance().world.getTimeOfDay() / 24000);

        path2 = this.rollbackDirectory.relativize(path2);
        path3 = this.rollbackDirectory.relativize(path3);
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
        if (!this.backupInfo.has(worldName))
            return true;

        JsonArray array = this.backupInfo.getAsJsonArray(worldName);
        if (index == -1)
            index += array.size();
        if (array.size() <= index)
            return true;

        RollbackBackup backup = new RollbackBackup(worldName, array.get(index).getAsJsonObject());

        try {
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.iconPath));
            Files.deleteIfExists(this.rollbackDirectory.resolve(backup.backupPath));
        } catch (IOException e) {
            showErrorToast("rollback.deleteBackup.failed", e);
            return false;
        }

        JsonArray oldArray = array.deepCopy();
        array.remove(index);
        try {
            saveBackupInfo();
        } catch (IOException e) {
            this.backupInfo.add(worldName, oldArray);
            showErrorToast("rollback.deleteBackup.failed", e);
            return false;
        }

        return true;
    }

    public List<RollbackBackup> getRollbacksFor(String worldName) {
        ArrayList<RollbackBackup> list = new ArrayList<>();
        if (!this.backupInfo.has(worldName))
            return list;

        JsonArray array = this.backupInfo.getAsJsonArray(worldName);
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

        Path source = this.rollbackDirectory.resolve(backup.backupPath.toString());
        Path dest = client.getLevelStorage().getSavesDirectory();
        try (ZipFile zip = new ZipFile(source.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = dest.resolve(entry.getName());
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

    public void deleteWorld(String worldName) {
        if (!this.backupInfo.has(worldName))
            return;

        while(this.backupInfo.getAsJsonArray(worldName).size() > 0)
            deleteBackup(worldName, 0);
        this.backupInfo.remove(worldName);

        try {
            this.saveBackupInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*public void cleanUp() {
        List<String> worldNames;
        try {
            LevelStorage storage = MinecraftClient.getInstance().getLevelStorage();
            LevelStorage.LevelList levelList = storage.getLevelList();
            List<LevelSummary> summaries = storage.loadSummaries(levelList).get();
            worldNames = summaries.stream().map(LevelSummary::getName).toList();
        } catch (Exception e) {
            return;
        }

        for (String world : this.backupInfo.keySet())
            if (!worldNames.contains(world))
                this.backupInfo.remove(world);

        List<Path> providedBackups;
        List<Path> providedIcons;
        try (Stream<Path> stream1 = Files.list(this.rollbackDirectory);
             Stream<Path> stream2 = Files.list(this.iconsDirectory)) {
            providedBackups = stream1.filter((path) -> !(Files.isDirectory(path)) || path.getFileName().toString().equals("rollbacks.json")).toList();
            providedIcons = stream2.toList();
        } catch (IOException e) {
            return;
        }

        ArrayList<String> expectedBackups = new ArrayList<>();
        ArrayList<String> expectedIcons = new ArrayList<>();
        for (String world : this.backupInfo.keySet()) {
            JsonArray backups = this.backupInfo.getAsJsonArray(world);
            for (JsonElement backup : backups) {
                String file = backup.getAsJsonObject().get("backup_file").getAsString();
                if (providedBackups.contains(Path.of(file))) {
                    expectedBackups.add(file);
                    expectedIcons.add(backup.getAsJsonObject().get("icon_file").getAsString());
                } else
                    backups.remove(backup);
            }
        }

        for (Path path : providedBackups) {
            if (!expectedBackups.contains(path.getFileName().toString())) {
                try {
                    Files.deleteIfExists(this.rollbackDirectory.resolve(path));
                } catch (IOException ignored) {}
            }
        }
        for (Path path : providedIcons) {
            if (!expectedIcons.contains(path.getFileName().toString())) {
                try {
                    Files.deleteIfExists(this.iconsDirectory.resolve(path));
                } catch (IOException ignored) {}
            }
        }
    }*/
}
