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
    private State state;

    public NetworkBackupManager(Minecraft client) {
        this.client = client;
        this.state = State.INITIAL;
    }

    public void loadingFinished(@NotNull SendMetadata.Metadata metadata) {
        this.world = metadata.world();
        this.defaultConfig = metadata.config();
        actionFinished();
    }

    public void actionFinished() {
        this.state = State.IDLE;
    }

    @Override
    public @NotNull State getCurrentState() {
        return this.state;
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
        this.state = State.LOADING;
        ClientPacketManager.send(Packets.fetchMetadata, null);
    }

    @Override public void deleteWorld() throws BackupManagerException { }

    @Override public void createBackup(String name, BackupType type) throws BackupManagerException { }

    @Override public void deleteBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void renameBackup(int backupID, BackupType type, String name) throws BackupManagerException { }

    @Override public void convertBackup(int backupID, BackupType from, BackupType to) throws BackupManagerException { }

    @Override public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void saveConfig() throws BackupManagerException { }

    @Override public void saveConfigAsDefault() throws BackupManagerException { }
}
