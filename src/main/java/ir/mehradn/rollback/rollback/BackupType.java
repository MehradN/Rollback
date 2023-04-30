package ir.mehradn.rollback.rollback;

public enum BackupType {
    AUTOMATED(false, false, true, true, true),
    COMMAND(true, true, true, true, true),
    MANUAL(true, false, false, false, false);

    public final boolean manualCreation;
    public final boolean manualDeletion;
    public final boolean automatedDeletion;
    public final boolean list;
    public final boolean rollback;

    BackupType(boolean manualCreation, boolean manualDeletion, boolean automatedDeletion, boolean list, boolean rollback) {
        assert !manualDeletion || automatedDeletion;
        assert !automatedDeletion || list;
        assert !rollback || list;

        this.manualCreation = manualCreation;
        this.manualDeletion = manualDeletion;
        this.automatedDeletion = automatedDeletion;
        this.list = list;
        this.rollback = rollback;
    }

    public String toString() {
        return super.toString().toLowerCase();
    }
}
