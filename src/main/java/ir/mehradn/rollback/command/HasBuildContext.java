package ir.mehradn.rollback.command;

public interface HasBuildContext {
    <T> T getContext(String key);
}
