package ir.mehradn.rollback.util.backup;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class RollbackBackup {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    public final String worldName;
    public final Path backupPath;
    public final Path iconPath;
    public final LocalDateTime backupTime;

    public RollbackBackup(String directoryName, JsonObject rollbackData) {
        worldName = directoryName;
        backupTime = LocalDateTime.parse(rollbackData.get("creation_date").getAsString(), TIME_FORMATTER);
        backupPath = Path.of(rollbackData.get("backup_file").getAsString());
        if (rollbackData.has("icon_file"))
            iconPath = Path.of(rollbackData.get("icon_file").getAsString());
        else
            iconPath = null;
    }

    public RollbackBackup(String worldName, Path backupPath, Path iconPath, LocalDateTime backupTime) {
        this.worldName = worldName;
        this.backupPath = backupPath;
        this.iconPath = iconPath;
        this.backupTime = backupTime;
    }

    public JsonObject toObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("creation_date", backupTime.format(TIME_FORMATTER));
        obj.addProperty("backup_file", backupPath.toString());
        if (iconPath != null)
            obj.addProperty("icon_file", iconPath.toString());
        return obj;
    }
}
