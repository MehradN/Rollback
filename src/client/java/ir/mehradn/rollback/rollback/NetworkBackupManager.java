package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.ClientPacketManager;
import ir.mehradn.rollback.network.packets.CreateBackup;
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
    private final Minecraft minecraft;
    @Nullable private RollbackWorld world = null;
    @Nullable private RollbackNetworkConfig defaultConfig = null;
    private int lastUpdateId = -1;
    private boolean loadingQueued = false;
    private State state;

    public NetworkBackupManager(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.state = State.INITIAL;
    }

    public void loadingFinished(@NotNull SendMetadata.MetadataReceive metadata) {
        this.lastUpdateId = metadata.lastUpdateId();
        this.world = metadata.world();
        this.defaultConfig = metadata.config();
        this.world.update(this);
        actionFinished();
    }

    public void actionFinished() {
        this.state = State.IDLE;
        if (this.loadingQueued)
            loadWorld();
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
    public @NotNull RollbackNetworkConfig getDefaultConfig() {
        Assertion.state(this.defaultConfig != null, "Call loadWorld and wait for loading to be finished!");
        return this.defaultConfig;
    }

    @Override
    public void loadWorld() {
        if (this.state == State.LOADING) {
            this.loadingQueued = true;
            return;
        }
        this.world = null;
        this.defaultConfig = null;
        this.state = State.LOADING;
        ClientPacketManager.send(Packets.fetchMetadata, this.minecraft.hasSingleplayerServer());
    }

    @Override
    public void createBackup(BackupType type, @Nullable String name) {
        if (name != null && (!type.list || name.isBlank()))
            name = null;
        if (name == null)
            name = "";
        Assertion.argument(name.length() <= MAX_NAME_LENGTH, "Backup name is too long");

        this.state = State.ACTION;
        ClientPacketManager.send(Packets.createBackup, new CreateBackup.Properties(this.lastUpdateId, type, name));
    }

    @Override public void deleteBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void renameBackup(int backupID, BackupType type, @Nullable String name) throws BackupManagerException { }

    @Override public void convertBackup(int backupID, BackupType from, BackupType to) throws BackupManagerException { }

    @Override public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException { }

    @Override public void saveConfig() throws BackupManagerException { }

    @Override public void saveConfigAsDefault() throws BackupManagerException { }
}
