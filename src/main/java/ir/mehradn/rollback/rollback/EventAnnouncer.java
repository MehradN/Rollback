package ir.mehradn.rollback.rollback;

public interface EventAnnouncer {
    void onError(String translatableTitle, String literalInfo);

    void onSuccessfulBackup(long size);

    void onSuccessfulDelete();

    void onSuccessfulConvert(BackupType from, BackupType to);

    void onSuccessfulConfig(boolean isDefault);
}
