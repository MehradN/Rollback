package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.network.FriendlyByteBuf;
import java.util.HashMap;
import java.util.Map;

public class RollbackWorld implements RollbackMetadata {
    @SerializedName("prompted") public boolean prompted = false;
    @SerializedName("days_passed") public int daysSinceLastBackup = 0;
    @SerializedName("since_day") public long ticksSinceLastMorning = 0;
    @SerializedName("since_backup") public long ticksSinceLastBackup = 0;
    @SerializedName("last_id") public int lastID = 0;
    @SerializedName("config") public RollbackWorldConfig config = new RollbackWorldConfig();
    @SerializedName("rollbacks") public Map<Integer, RollbackBackup> automatedBackups = new HashMap<>();
    @SerializedName("backups") public Map<Integer, RollbackBackup> commandBackups = new HashMap<>();

    public RollbackBackup getBackup(int backupID, BackupType type) {
        Assertion.argument(type.listing, "Invalid type!");
        Map<Integer, RollbackBackup> backups = getBackups(type);
        Assertion.argument(backups.containsKey(backupID), "Invalid backupID!");
        return backups.get(backupID);
    }

    public Map<Integer, RollbackBackup> getBackups(BackupType type) {
        Assertion.argument(type.listing, "Invalid type!");
        return switch (type) {
            case ROLLBACK -> this.automatedBackups;
            case BACKUP -> this.commandBackups;
            default -> new HashMap<>();
        };
    }

    @Override
    public void update(BackupManager backupManager) {
        this.config.update(backupManager);
        for (RollbackBackup backup : this.automatedBackups.values())
            backup.update(backupManager);
        for (RollbackBackup backup : this.commandBackups.values())
            backup.update(backupManager);
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        buf.writeBoolean(this.prompted);
        buf.writeInt(this.daysSinceLastBackup);
        buf.writeLong(this.ticksSinceLastMorning);
        buf.writeLong(this.ticksSinceLastBackup);
        buf.writeInt(this.lastID);

        this.config.writeToBuf(buf, integrated);
        writeMapToBuf(this.automatedBackups, buf, integrated);
        writeMapToBuf(this.commandBackups, buf, integrated);
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        this.prompted = buf.readBoolean();
        this.daysSinceLastBackup = buf.readInt();
        this.ticksSinceLastMorning = buf.readLong();
        this.ticksSinceLastBackup = buf.readLong();
        this.lastID = buf.readInt();

        this.config.readFromBuf(buf);
        readMapFromBuf(this.automatedBackups, buf);
        readMapFromBuf(this.commandBackups, buf);
    }

    private void writeMapToBuf(Map<Integer, RollbackBackup> map, FriendlyByteBuf buf, boolean integrated) {
        buf.writeInt(map.size());
        for (Map.Entry<Integer, RollbackBackup> entry : map.entrySet()) {
            buf.writeInt(entry.getKey());
            entry.getValue().writeToBuf(buf, integrated);
        }
    }

    private void readMapFromBuf(Map<Integer, RollbackBackup> map, FriendlyByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            int key = buf.readInt();
            RollbackBackup value = new RollbackBackup();
            value.readFromBuf(buf);
            map.put(key, value);
        }
    }
}
