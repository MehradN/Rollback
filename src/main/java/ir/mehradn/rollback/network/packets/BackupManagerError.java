package ir.mehradn.rollback.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class BackupManagerError extends Packet<BackupManagerError.Info, BackupManagerError.Info> {
    private static final int MAX_STRING_LENGTH = 1023;

    BackupManagerError() {
        super("on_error");
    }

    @Override
    public FriendlyByteBuf toBuf(Info data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(data.translatableTitle, MAX_STRING_LENGTH);
        buf.writeUtf(data.literalInfo, MAX_STRING_LENGTH);
        return buf;
    }

    @Override
    public Info fromBuf(FriendlyByteBuf buf) {
        String title = buf.readUtf(MAX_STRING_LENGTH);
        String info = buf.readUtf(MAX_STRING_LENGTH);
        return new Info(title, info);
    }

    public record Info(String translatableTitle, String literalInfo) { }
}
