package ir.mehradn.rollback.util.backup;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Date;

public class RollbackBackup {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_')
        .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    public final String worldName;
    public final Path backupPath;
    public final Path iconPath;
    public final LocalDateTime backupTime;
    public final int daysPlayed;

    public RollbackBackup(String directoryName, JsonObject rollbackData) {
        this.worldName = directoryName;
        this.backupTime = LocalDateTime.parse(rollbackData.get("creation_date").getAsString(), TIME_FORMATTER);
        this.daysPlayed = rollbackData.get("days_played").getAsInt();
        this.backupPath = Path.of(rollbackData.get("backup_file").getAsString());
        if (rollbackData.has("icon_file"))
            this.iconPath = Path.of(rollbackData.get("icon_file").getAsString());
        else
            this.iconPath = null;
    }

    public RollbackBackup(String worldName, Path backupPath, Path iconPath, LocalDateTime backupTime, int daysPlayed) {
        this.worldName = worldName;
        this.backupPath = backupPath;
        this.iconPath = iconPath;
        this.backupTime = backupTime;
        this.daysPlayed = daysPlayed;
    }

    public JsonObject toObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("creation_date", this.backupTime.format(TIME_FORMATTER));
        obj.addProperty("days_played", this.daysPlayed);
        obj.addProperty("backup_file", this.backupPath.toString());
        if (this.iconPath != null)
            obj.addProperty("icon_file", this.iconPath.toString());
        return obj;
    }

    public String getDateAsString() {
        Date date = Date.from(this.backupTime.toInstant(ZoneOffset.UTC));
        return DATE_FORMAT.format(date);
    }
}
