package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import java.util.HashMap;
import java.util.Map;

public class RollbackWorld implements UpdatesAfterLoading {
    @SerializedName("prompted") public boolean prompted = false;
    @SerializedName("days_passed") public int daysSinceLastBackup = 0;
    @SerializedName("since_day") public long ticksSinceLastMorning = 0;
    @SerializedName("since_backup") public long ticksSinceLastBackup = 0;
    @SerializedName("last_id") public int lastID = 0;
    @SerializedName("config") public RollbackWorldConfig config = new RollbackWorldConfig();
    @SerializedName("rollbacks") public Map<Integer, RollbackBackup> automatedBackups = new HashMap<>();
    @SerializedName("backups") public Map<Integer, RollbackBackup> commandBackups = new HashMap<>();

    public RollbackBackup getBackup(int backupID, BackupType type) {
        Assertion.argument(type.list, "Invalid type!");
        Map<Integer, RollbackBackup> backups = getBackups(type);
        Assertion.argument(backups.containsKey(backupID), "Invalid backupID!");
        return backups.get(backupID);
    }

    public Map<Integer, RollbackBackup> getBackups(BackupType type) {
        Assertion.argument(type.list, "Invalid type!");
        return switch (type) {
            case AUTOMATED -> this.automatedBackups;
            case COMMAND -> this.commandBackups;
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
}
