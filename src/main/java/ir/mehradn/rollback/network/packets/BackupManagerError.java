package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class BackupManagerError implements FabricPacket {
    private static final int MAX_STRING_LENGTH = 1023;
    public static final PacketType<BackupManagerError> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "on_error"),
        BackupManagerError::new
    );
    public final String translatableTitle;
    public final String literalInfo;

    public BackupManagerError(String translatableTitle, String literalInfo) {
        this.translatableTitle = translatableTitle;
        this.literalInfo = literalInfo;
    }

    public BackupManagerError(FriendlyByteBuf buf) {
        this.translatableTitle = buf.readUtf(MAX_STRING_LENGTH);
        this.literalInfo = buf.readUtf(MAX_STRING_LENGTH);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.translatableTitle, MAX_STRING_LENGTH);
        buf.writeUtf(this.literalInfo, MAX_STRING_LENGTH);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
