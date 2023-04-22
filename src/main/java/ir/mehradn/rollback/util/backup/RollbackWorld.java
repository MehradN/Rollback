package ir.mehradn.rollback.util.backup;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RollbackWorld {
    @SerializedName("automated") public boolean automatedBackups = false;
    @SerializedName("prompted") public boolean prompted = false;
    @SerializedName("days_passed") public int daysSinceLastBackup = 0;
    @SerializedName("since_day") public int ticksSinceLastMorning = 0;
    @SerializedName("since_backup") public int ticksSinceLastBackup = 0;
    @SerializedName("backups") public List<RollbackBackup> backups = new ArrayList<>();

    public void setPromptAnswer(boolean answer) {
        this.prompted = true;
        this.automatedBackups = answer;
    }

    public void resetTimers() {
        this.daysSinceLastBackup = 0;
        this.ticksSinceLastMorning = 0;
        this.ticksSinceLastBackup = 0;
    }
}
