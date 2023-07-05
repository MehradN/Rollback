package ir.mehradn.rollback.rollback;

import net.minecraft.network.chat.Component;

public enum BackupType {
    ROLLBACK(false, false, true),
    BACKUP(true, true, true),
    MANUAL(true, false, false);
    public final boolean creation;
    public final boolean deletion;
    public final boolean listing;

    BackupType(boolean creation, boolean deletion, boolean listing) {
        this.creation = creation;
        this.deletion = deletion;
        this.listing = listing;
    }

    public Component toComponent() {
        return Component.translatable("rollback." + this);
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
