package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class NetworkBackupManager implements BackupManager {
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

    public void loadingFinished(@NotNull SendMetadata packet) {
        Assertion.argument(packet.worldMetadata != null && packet.defaultConfig != null);
        this.lastUpdateId = packet.lastUpdateId;
        this.world = packet.worldMetadata;
        this.defaultConfig = packet.defaultConfig;
        this.world.update(this);
        actionFinished();
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
        this.loadingQueued = false;
        this.world = null;
        this.defaultConfig = null;
        this.state = State.LOADING;
        ClientPlayNetworking.send(new FetchMetadata(ScreenManager.isIntegrated(this.minecraft)));
    }

    @Override
    public void createBackup(BackupType type, @Nullable String name) {
        if (name == null || (!type.listing || name.isBlank()))
            name = "";
        Assertion.argument(name.length() <= MAX_NAME_LENGTH, "Backup name is too long");

        this.state = State.ACTION;
        ClientPlayNetworking.send(new CreateBackup(this.lastUpdateId, type, name));
    }

    @Override
    public void deleteBackup(int backupID, BackupType type) {
        Assertion.argument(type.deletion, "Invalid type");
        this.state = State.ACTION;
        ClientPlayNetworking.send(new DeleteBackup(this.lastUpdateId, backupID, type));
    }

    @Override
    public void renameBackup(int backupID, BackupType type, @Nullable String name) {
        Assertion.argument(type.listing, "Invalid type");
        if (name == null || name.isBlank())
            name = "";
        Assertion.argument(name.length() <= MAX_NAME_LENGTH, "Backup name is too long");

        this.state = State.ACTION;
        ClientPlayNetworking.send(new RenameBackup(this.lastUpdateId, backupID, type, name));
    }

    @Override
    public void convertBackup(int backupID, BackupType from, BackupType to) {
        Assertion.argument(from.listing && to.creation && from != to, "Invalid types!");
        this.state = State.ACTION;
        ClientPlayNetworking.send(new ConvertBackup(this.lastUpdateId, backupID, from, to));
    }

    @Override
    public void rollbackToBackup(int backupID, BackupType type) {
        Assertion.argument(type.listing, "Invalid type");
        this.state = State.ACTION;
        ClientPlayNetworking.send(new RollbackBackup(this.lastUpdateId, backupID, type));
    }

    @Override
    public void saveConfig() {
        this.state = State.ACTION;
        ClientPlayNetworking.send(new SaveConfig(this.lastUpdateId, false, this.getWorld().config));
    }

    @Override
    public void saveConfigAsDefault() {
        this.state = State.ACTION;
        ClientPlayNetworking.send(new SaveConfig(this.lastUpdateId, true, this.getWorld().config));
    }

    private void actionFinished() {
        this.state = State.IDLE;
        if (this.loadingQueued)
            loadWorld();
    }
}
