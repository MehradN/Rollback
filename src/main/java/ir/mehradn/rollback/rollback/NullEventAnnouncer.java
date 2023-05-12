package ir.mehradn.rollback.rollback;

public class NullEventAnnouncer implements EventAnnouncer {
    @Override
    public void onError(String translatableTitle, String literalInfo) { }

    @Override
    public void onSuccessfulBackup(long size) { }

    @Override
    public void onSuccessfulDelete() { }

    @Override
    public void onSuccessfulConvert(BackupType from, BackupType to) { }

    @Override
    public void onSuccessfulConfig(boolean isDefault) { }
}
