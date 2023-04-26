package ir.mehradn.rollback.rollback.exception;

public class BackupManagerException extends Exception {
    public BackupManagerException(String message) {
        super(message);
    }

    public BackupManagerException(Throwable cause) {
        super(cause);
    }

    public BackupManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
