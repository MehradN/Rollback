package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class RollbackBackup implements UpdatesAfterLoading {
    @SerializedName("backup_file") public Path backupPath = null;
    @SerializedName("icon_file") public Path iconPath = null;
    @SerializedName("creation_date") public LocalDateTime creationDate = null;
    @SerializedName("days_played") public int daysPlayed = -1;
    @SerializedName("file_size") public long fileSize = -1;
    @SerializedName("name") public String name = null;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    public String getDaysPlayedAsString() {
        return (this.daysPlayed == -1 ? "???" : String.valueOf(this.daysPlayed));
    }

    public String getFileSizeAsString() {
        return (this.fileSize == -1 ? "???" : FileUtils.byteCountToDisplaySize(this.fileSize));
    }

    public String getDateAsString() {
        if (this.creationDate == null)
            return "???";
        Date date = Date.from(this.creationDate.atZone(ZoneId.systemDefault()).toInstant());
        return DATE_FORMAT.format(date);
    }

    @Override
    public void update(BackupManager backupManager) {
        if (this.fileSize != -1 || !(backupManager instanceof CommonBackupManager cbm))
            return;
        try {
            Path path = cbm.getRollbackDirectory().resolve(this.backupPath);
            this.fileSize = Files.size(path);
        } catch (IOException ignored) { }
    }
}
