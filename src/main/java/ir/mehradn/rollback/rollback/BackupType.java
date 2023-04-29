package ir.mehradn.rollback.rollback;

public enum BackupType {
    AUTOMATED,
    COMMAND;

    public String getName() {
        return this.name().toLowerCase();
    }
}
