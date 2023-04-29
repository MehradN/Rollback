package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.rollback.BackupType;
import java.util.HashMap;
import java.util.Map;

public class RollbackWorld {
    @SerializedName("automated") public boolean automatedBackupsEnabled = false;
    @SerializedName("prompted") public boolean prompted = false;
    @SerializedName("days_passed") public int daysSinceLastBackup = 0;
    @SerializedName("since_day") public int ticksSinceLastMorning = 0;
    @SerializedName("since_backup") public int ticksSinceLastBackup = 0;
    @SerializedName("last_id") public int lastID = 0;
    @SerializedName("rollbacks") public Map<Integer, RollbackBackup> automatedBackups = new HashMap<>();
    @SerializedName("backups") public Map<Integer, RollbackBackup> commandBackups = new HashMap<>();

    public RollbackBackup getBackup(int backupID, BackupType type) {
        Map<Integer, RollbackBackup> backups = getBackups(type);
        if (!backups.containsKey(backupID))
            throw new IllegalArgumentException("Invalid backupID!");
        return backups.get(backupID);
    }

    public Map<Integer, RollbackBackup> getBackups(BackupType type) {
        Map<Integer, RollbackBackup> backups;
        switch (type) {
            case AUTOMATED -> backups = this.automatedBackups;
            case COMMAND -> backups = this.commandBackups;
            default -> backups = new HashMap<>();
        }
        return backups;
    }
}
