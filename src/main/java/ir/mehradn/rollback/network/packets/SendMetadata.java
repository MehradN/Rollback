package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public final class SendMetadata extends Packet<SendMetadata.MetadataSend, SendMetadata.MetadataReceive> {
    SendMetadata() {
        super("send_metadata");
    }

    @Override
    public FriendlyByteBuf toBuf(MetadataSend data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        data.version.writeToBuf(buf, data.integrated);
        data.config.writeToBuf(buf, data.integrated);
        data.world.writeToBuf(buf, data.integrated);
        return buf;
    }

    @Override
    public @Nullable MetadataReceive fromBuf(FriendlyByteBuf buf) {
        RollbackVersion version = RollbackVersion.fromBuf(buf);
        if (version.notMatch())
            return null;
        RollbackNetworkConfig config = new RollbackNetworkConfig();
        config.readFromBuf(buf);
        RollbackWorld world = new RollbackWorld();
        world.readFromBuf(buf);
        return new MetadataReceive(version, world, config);
    }

    public record MetadataSend(boolean integrated, RollbackVersion version, RollbackWorld world, RollbackNetworkConfig config) { }

    public record MetadataReceive(RollbackVersion version, RollbackWorld world, RollbackNetworkConfig config) { }
}
