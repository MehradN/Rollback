package ir.mehradn.rollback.rollback;

public class NullEventAnnouncer implements EventAnnouncer {
    public void onError(String translatableTitle, String literalInfo) { }

    public void onSuccessfulBackup(long size) { }
}
