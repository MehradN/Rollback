package ir.mehradn.rollback.network.packet;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SuccessfulConfig implements FabricPacket {
    public static final PacketType<SuccessfulConfig> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "successful_config"),
        SuccessfulConfig::new
    );

    public SuccessfulConfig() { }

    public SuccessfulConfig(FriendlyByteBuf buf) { }

    @Override
    public void write(FriendlyByteBuf buf) { }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
