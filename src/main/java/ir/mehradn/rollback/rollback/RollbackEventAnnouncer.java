package ir.mehradn.rollback.rollback;

public interface RollbackEventAnnouncer {
    void onError(String translatableTitle, String literalInfo);

    void onSuccessfulBackup(long size);
}
