package ir.mehradn.rollback.config;

public enum ConfigType {
    WORLD,
    DEFAULT;

    public String toString() {
        return super.toString().toLowerCase();
    }
}
