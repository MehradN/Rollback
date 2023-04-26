package ir.mehradn.rollback.rollback.exception;

public class BackupMinecraftException extends BackupManagerException {
    public BackupMinecraftException(String message) {
        super(message);
    }

    public BackupMinecraftException(Throwable cause) {
        super(cause);
    }

    public BackupMinecraftException(String message, Throwable cause) {
        super(message, cause);
    }
}
