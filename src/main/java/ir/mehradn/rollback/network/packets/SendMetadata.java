package ir.mehradn.rollback.network.packets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.network.JsonCompressor;
import ir.mehradn.rollback.network.RollbackNetworkConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class SendMetadata extends Packet<SendMetadata.MetadataSend, SendMetadata.MetadataReceive> {
    private static final Gson GSON = new Gson();

    SendMetadata() {
        super("send_metadata");
    }

    @Override
    public FriendlyByteBuf toBuf(MetadataSend data) {
        JsonElement json = data.toJson();
        String string = GSON.toJson(json);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(string);
        return buf;
    }

    @Override
    public MetadataReceive fromBuf(FriendlyByteBuf buf) {
        String string = buf.readUtf();
        JsonElement json = GSON.fromJson(string, JsonElement.class);
        return MetadataReceive.fromJson(json);
    }

    public record MetadataSend(boolean integrated, RollbackWorld world, RollbackDefaultConfig config) {
        JsonElement toJson() {
            RollbackNetworkConfig networkConfig = new RollbackNetworkConfig();
            networkConfig.copyFrom(this.config);

            JsonElement worldJson = BackupManager.GSON.toJsonTree(this.world, RollbackWorld.class);
            JsonElement configJson = networkConfig.toJson();
            JsonObject json = new JsonObject();
            json.add("world", worldJson);
            json.add("defaultConfig", configJson);

            JsonCompressor compressor = new JsonCompressor(this.integrated);
            return compressor.compress(json);
        }
    }

    public record MetadataReceive(RollbackWorld world, RollbackNetworkConfig config) {
        static MetadataReceive fromJson(JsonElement json) {
            JsonCompressor compressor = new JsonCompressor(false);
            JsonObject object = compressor.decompress(json).getAsJsonObject();
            JsonElement worldJson = object.get("world");
            JsonElement configJson = object.get("defaultConfig");

            RollbackWorld world = BackupManager.GSON.fromJson(worldJson, RollbackWorld.class);
            RollbackNetworkConfig config = RollbackNetworkConfig.fromJson(configJson);
            return new MetadataReceive(world, config);
        }
    }
}
