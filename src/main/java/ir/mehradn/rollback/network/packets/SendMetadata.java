package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SendMetadata implements FabricPacket {
    public static final PacketType<SendMetadata> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "send_metadata"),
        SendMetadata::new
    );
    public final RollbackVersion version;
    public final int lastUpdateId;
    public final RollbackNetworkConfig defaultConfig;
    public final RollbackWorld worldMetadata;
    private boolean integrated = false;

    public SendMetadata(RollbackVersion version, int lastUpdateId, RollbackNetworkConfig defaultConfig, RollbackWorld worldMetadata) {
        this.version = version;
        this.lastUpdateId = lastUpdateId;
        this.defaultConfig = defaultConfig;
        this.worldMetadata = worldMetadata;
    }

    public SendMetadata(FriendlyByteBuf buf) {
        this.version = RollbackVersion.fromBuf(buf);
        if (this.version.notMatch()) {
            this.lastUpdateId = 0;
            this.defaultConfig = null;
            this.worldMetadata = null;
            return;
        }
        this.lastUpdateId = buf.readInt();
        this.defaultConfig = new RollbackNetworkConfig();
        this.defaultConfig.readFromBuf(buf);
        this.worldMetadata = new RollbackWorld();
        this.worldMetadata.readFromBuf(buf);
    }

    public SendMetadata setIntegrated(boolean integrated) {
        this.integrated = integrated;
        return this;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.version.writeToBuf(buf, this.integrated);
        buf.writeInt(this.lastUpdateId);
        this.defaultConfig.writeToBuf(buf, this.integrated);
        this.worldMetadata.writeToBuf(buf, this.integrated);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
