package ir.mehradn.rollback.rollback.exception;

public class BackupIOException extends BackupManagerException {
    public BackupIOException(String message) {
        super(message);
    }

    public BackupIOException(Throwable cause) {
        super(cause);
    }

    public BackupIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
