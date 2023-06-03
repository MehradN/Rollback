package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.rollback.BackupManager;
import net.minecraft.network.FriendlyByteBuf;

public interface RollbackMetadata {
    default void update(BackupManager backupManager) { }

    default void writeToBuf(FriendlyByteBuf buf, boolean integrated) { }

    default void readFromBuf(FriendlyByteBuf buf) { }
}
