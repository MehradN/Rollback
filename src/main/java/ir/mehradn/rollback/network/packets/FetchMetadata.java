package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class FetchMetadata implements FabricPacket {
    public static final PacketType<FetchMetadata> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "fetch_metadata"),
        FetchMetadata::new
    );
    public final boolean integrated;

    public FetchMetadata(boolean integrated) {
        this.integrated = integrated;
    }

    public FetchMetadata(FriendlyByteBuf buf) {
        this.integrated = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.integrated);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
