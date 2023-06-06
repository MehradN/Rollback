package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import net.minecraft.network.FriendlyByteBuf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class RollbackBackup implements RollbackMetadata {
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

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        boolean[] c = new boolean[]{
            integrated && this.iconPath != null,
            this.name != null,
            this.creationDate != null
        };
        Packets.writeBooleanArray(buf, c);

        if (c[0])
            Packets.writeString(buf, this.iconPath.toString());
        if (c[1])
            Packets.writeString(buf, this.name);
        if (c[2])
            buf.writeLong(this.creationDate.toEpochSecond(ZoneOffset.UTC));
        buf.writeInt(this.daysPlayed);
        buf.writeLong(this.fileSize);
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        boolean[] c = Packets.readBooleanArray(buf, 3);

        if (c[0])
            this.iconPath = Path.of(Packets.readString(buf));
        if (c[1])
            this.name = Packets.readString(buf);
        if (c[2])
            this.creationDate = LocalDateTime.ofEpochSecond(buf.readLong(), 0, ZoneOffset.UTC);
        this.daysPlayed = buf.readInt();
        this.fileSize = buf.readLong();
    }
}
