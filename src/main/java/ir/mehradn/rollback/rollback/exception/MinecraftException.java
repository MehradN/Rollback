package ir.mehradn.rollback.rollback.exception;

public class MinecraftException extends Exception {
    public MinecraftException(String message) {
        super(message);
    }

    public MinecraftException(Throwable cause) {
        super(cause);
    }

    public MinecraftException(String message, Throwable cause) {
        super(message, cause);
    }
}
