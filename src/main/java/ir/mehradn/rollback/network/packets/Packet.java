package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public abstract class Packet <S, R> {
    public final ResourceLocation identifier;

    protected Packet(String location) {
        this.identifier = new ResourceLocation(Rollback.MOD_ID, location);
    }

    public abstract FriendlyByteBuf toBuf(S data);

    public abstract R fromBuf(FriendlyByteBuf buf);
}
