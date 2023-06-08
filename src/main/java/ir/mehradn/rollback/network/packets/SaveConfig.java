package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SaveConfig implements FabricPacket {
    public static final PacketType<SaveConfig> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "save_config"),
        SaveConfig::new
    );
    public final int lastUpdateId;
    public final boolean saveAsDefault;
    public final RollbackWorldConfig worldConfig;

    public SaveConfig(int lastUpdateId, boolean saveAsDefault, RollbackWorldConfig worldConfig) {
        this.lastUpdateId = lastUpdateId;
        this.saveAsDefault = saveAsDefault;
        this.worldConfig = worldConfig;
    }

    public SaveConfig(FriendlyByteBuf buf) {
        this.lastUpdateId = buf.readInt();
        this.saveAsDefault = buf.readBoolean();
        this.worldConfig = new RollbackWorldConfig();
        this.worldConfig.readFromBuf(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.lastUpdateId);
        buf.writeBoolean(this.saveAsDefault);
        this.worldConfig.writeToBuf(buf, false);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
