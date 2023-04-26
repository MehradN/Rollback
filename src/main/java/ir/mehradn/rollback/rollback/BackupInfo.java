package ir.mehradn.rollback.rollback;

import java.nio.file.Path;

public record BackupInfo(Path backupPath, long size) { }
