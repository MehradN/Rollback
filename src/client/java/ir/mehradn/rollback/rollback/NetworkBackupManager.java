package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.ClientPacketManager;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.network.packets.SendMetadata;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class NetworkBackupManager implements BackupManager {
    private final Minecraft client;
    @Nullable private RollbackWorld world = null;
    @Nullable private RollbackDefaultConfig defaultConfig = null;
    private boolean loading = false;

    public NetworkBackupManager(Minecraft client) {
        this.client = client;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public void loadingFinished(@NotNull SendMetadata.Metadata metadata) {
        this.world = metadata.world();
        this.defaultConfig = metadata.config();
        this.loading = false;
    }

    @Override
    public @NotNull RollbackWorld getWorld() {
        Assertion.state(this.world != null, "Call loadWorld and wait for loading to be finished!");
        return this.world;
    }

    @Override
    public @NotNull RollbackDefaultConfig getDefaultConfig() {
        Assertion.state(this.defaultConfig != null, "Call loadWorld and wait for loading to be finished!");
        return this.defaultConfig;
    }

    @Override
    public void loadWorld() {
        this.world = null;
        this.defaultConfig = null;
        this.loading = true;
        ClientPacketManager.send(Packets.fetchMetadata, null);
    }

    @Override public void saveWorld() throws BackupManagerException { }

    @Override public void deleteWorld() throws BackupManagerException { }

    @Override public void createBackup(String name, BackupType type) throws BackupManagerException { }

    @Override public void deleteBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void convertBackup(int backupID, BackupType from, String name, BackupType to) throws BackupManagerException { }

    @Override public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void saveConfig() throws BackupManagerException { }

    @Override public void saveConfigAsDefault() throws BackupManagerException { }
}
