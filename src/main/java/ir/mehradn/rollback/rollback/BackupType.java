package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.exception.Assertion;

public enum BackupType {
    AUTOMATED(false, true, true, true, true, true, false),
    COMMAND(true, true, true, true, true, true, true),
    MANUAL(true, false, false, false, false, false, true);
    public final boolean manualCreation;
    public final boolean manualDeletion;
    public final boolean automatedDeletion;
    public final boolean list;
    public final boolean rollback;
    public final boolean convertFrom;
    public final boolean convertTo;

    BackupType(boolean manualCreation, boolean manualDeletion, boolean automatedDeletion, boolean list, boolean rollback, boolean convertFrom,
               boolean convertTo) {
        Assertion.argument(ifAThenB(convertTo, manualCreation) &&
                           ifAThenB(convertFrom, manualDeletion) &&
                           ifAThenB(manualDeletion, automatedDeletion) &&
                           ifAThenB(automatedDeletion, list) &&
                           ifAThenB(rollback, list));

        this.manualCreation = manualCreation;
        this.manualDeletion = manualDeletion;
        this.automatedDeletion = automatedDeletion;
        this.list = list;
        this.rollback = rollback;
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
    }

    public String toString() {
        return super.toString().toLowerCase();
    }

    private boolean ifAThenB(boolean a, boolean b) {
        return !a || b;
    }
}
