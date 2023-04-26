package ir.mehradn.rollback.rollback.exception;

public interface ExceptionBuilder <EX> {
    EX build(String message, Throwable cause);
}
