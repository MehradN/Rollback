package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.ConfigType;

public interface EventAnnouncer {
    void onError(String translatableTitle, String literalInfo);

    void onSuccessfulBackup(BackupType type, long size);

    void onSuccessfulDelete(int backupID, BackupType type);

    void onSuccessfulConvert(int backupID, BackupType from, BackupType to);

    void onSuccessfulConfig(ConfigType type);
}
