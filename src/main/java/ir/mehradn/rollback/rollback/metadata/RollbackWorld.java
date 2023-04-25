package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
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

    public RollbackBackup getBackup(int id, BackupType type) {
        RollbackBackup backup;
        switch (type) {
            case AUTOMATED -> backup = this.automatedBackups.get(id);
            case COMMAND -> backup = this.commandBackups.get(id);
            default -> backup = null;
        }
        return backup;
    }

    public void addBackup(RollbackBackup backup, BackupType type) {
        int id = ++this.lastID;
        switch (type) {
            case AUTOMATED -> this.automatedBackups.put(id, backup);
            case COMMAND -> this.commandBackups.put(id, backup);
        }
    }

    public enum BackupType {
        AUTOMATED,
        COMMAND
    }
}
