package ir.mehradn.rollback.exception;

import org.jetbrains.annotations.NotNull;

public class BackupManagerException extends Exception {
    public final BMECause cause;

    public BackupManagerException(BMECause cause, String message) {
        super(message);
        this.cause = cause;
    }

    public BackupManagerException(BMECause cause, String message, @NotNull Throwable exception) {
        super(message, exception);
        this.cause = cause;
    }

    public BackupManagerException(String message, BackupManagerException cause) {
        super(message, cause);
        this.cause = cause.cause;
    }
}
