package ir.mehradn.rollback.util.backup;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Environment(EnvType.CLIENT)
public class RollbackBackup {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    @SerializedName("backup_file") public Path backupPath;
    @SerializedName("icon_file") public Path iconPath = null;
    @SerializedName("creation_date") public LocalDateTime creationDate;
    @SerializedName("days_played") public int daysPlayed = -1;
    @SerializedName("name") public String name = null;

    public RollbackBackup() { }

    public RollbackBackup(Path backupPath, Path iconPath, LocalDateTime creationDate, int daysPlayed, String name) {
        this.backupPath = backupPath;
        this.iconPath = iconPath;
        this.creationDate = creationDate;
        this.daysPlayed = daysPlayed;
        this.name = name;
    }

    public String getDaysPlayedAsString() {
        return (this.daysPlayed == -1 ? "???" : String.valueOf(this.daysPlayed));
    }

    public String getDateAsString() {
        Date date = Date.from(this.creationDate.atZone(ZoneId.systemDefault()).toInstant());
        return DATE_FORMAT.format(date);
    }
}
