package ir.mehradn.rollback.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import java.util.BitSet;

public interface Packets {
    BackupManagerError backupManagerError = new BackupManagerError();
    CreateBackup createBackup = new CreateBackup();
    FetchMetadata fetchMetadata = new FetchMetadata();
    NewUpdateId newUpdateId = new NewUpdateId();
    OpenGUI openGui = new OpenGUI();
    SendMetadata sendMetadata = new SendMetadata();
    int MAX_ENCODED_STRING_LENGTH = 63;

    static void writeBooleanArray(FriendlyByteBuf buf, boolean[] array) {
        BitSet bitset = new BitSet(array.length);
        for (int i = 0; i < array.length; i++)
            bitset.set(i, array[i]);
        buf.writeBitSet(bitset);
    }

    static boolean[] readBooleanArray(FriendlyByteBuf buf, int size) {
        BitSet bitset = buf.readBitSet();
        boolean[] array = new boolean[size];
        for (int i = 0; i < Math.min(size, bitset.size()); i++)
            array[i] = bitset.get(i);
        return array;
    }

    static void writeString(FriendlyByteBuf buf, String string) {
        if (string.length() > MAX_ENCODED_STRING_LENGTH)
            string = "";
        buf.writeUtf(string, MAX_ENCODED_STRING_LENGTH);
    }

    static String readString(FriendlyByteBuf buf) {
        return buf.readUtf(MAX_ENCODED_STRING_LENGTH);
    }
}
