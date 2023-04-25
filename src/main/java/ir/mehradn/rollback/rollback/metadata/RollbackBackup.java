package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class RollbackBackup {
    @SerializedName("backup_file") public Path backupPath = null;
    @SerializedName("icon_file") public Path iconPath = null;
    @SerializedName("creation_date") public LocalDateTime creationDate = null;
    @SerializedName("days_played") public int daysPlayed = -1;
    @SerializedName("name") public String name = null;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    public String getDaysPlayedAsString() {
        return (this.daysPlayed == -1 ? "???" : String.valueOf(this.daysPlayed));
    }

    public String getDateAsString() {
        if (this.creationDate == null)
            return "???";
        Date date = Date.from(this.creationDate.atZone(ZoneId.systemDefault()).toInstant());
        return DATE_FORMAT.format(date);
    }
}
