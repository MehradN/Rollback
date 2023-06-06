package ir.mehradn.rollback.exception;

import org.jetbrains.annotations.NotNull;

public class BackupManagerException extends Exception {
    public final Cause cause;

    public BackupManagerException(Cause cause, String message) {
        super(message);
        this.cause = cause;
    }

    public BackupManagerException(Cause cause, String message, @NotNull Throwable exception) {
        super(message, exception);
        this.cause = cause;
    }

    public BackupManagerException(String message, BackupManagerException cause) {
        super(message, cause);
        this.cause = cause.cause;
    }

    public enum Cause {
        IO_EXCEPTION,
        MINECRAFT_FAILURE
    }
}
