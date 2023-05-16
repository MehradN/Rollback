package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public abstract class Packet <T> {
    public final ResourceLocation identifier;

    protected Packet(String location) {
        this.identifier = new ResourceLocation(Rollback.MOD_ID, location);
    }

    public abstract FriendlyByteBuf toBuf(T data);

    public abstract T fromBuf(FriendlyByteBuf buf);
}
