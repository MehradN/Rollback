package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class NewUpdateId implements FabricPacket {
    public static final PacketType<NewUpdateId> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "new_update_id"),
        NewUpdateId::new
    );

    public NewUpdateId() { }

    public NewUpdateId(FriendlyByteBuf buf) { }

    @Override
    public void write(FriendlyByteBuf buf) { }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
