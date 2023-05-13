package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.ConfigType;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;

public class ServerEventAnnouncer implements EventAnnouncer {
    private final MinecraftServer server;

    public ServerEventAnnouncer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onError(String translatableTitle, String literalInfo) {
        this.server.sendSystemMessage(Component.translatable(translatableTitle).append("\n" + literalInfo));
    }

    @Override
    public void onSuccessfulBackup(BackupType type, long size) {
        String str = FileUtils.byteCountToDisplaySize(size);
        this.server.sendSystemMessage(Component.translatable("rollback.success.createBackup", type.toString(), str));
    }

    @Override
    public void onSuccessfulDelete(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.deleteBackup", backupID, type));
    }

    @Override
    public void onSuccessfulConvert(int backupID, BackupType from, BackupType to) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.convertBackup", backupID, from, to));
    }

    @Override
    public void onSuccessfulConfig(ConfigType type) {
        CommonBackupManager backupManager = ((MinecraftServerExpanded)this.server).getBackupManager();
        RollbackConfig config = switch (type) {
            case WORLD -> backupManager.getDefaultConfig();
            case DEFAULT -> backupManager.getWorld().config;
        };

        MutableComponent component = Component.translatable("rollback.success.updateConfig." + type);
        for (ConfigEntry<?> entry : config.getEntries())
            component.append("\n" + entry.name + " = " + entry.getAsString());
        this.server.sendSystemMessage(component);
    }
}
