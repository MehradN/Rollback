package ir.mehradn.rollback.network.packets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.network.JsonCompressor;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class SendMetadata extends Packet<SendMetadata.Metadata> {
    private static final JsonCompressor COMPRESSOR = new JsonCompressor(false);
    private static final Gson GSON = new Gson();

    SendMetadata() {
        super("send_metadata");
    }

    @Override
    public FriendlyByteBuf toBuf(Metadata data) {
        JsonElement json = data.toJson();
        JsonElement compressed = COMPRESSOR.compress(json);
        String string = GSON.toJson(compressed);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(string);
        return buf;
    }

    @Override
    public Metadata fromBuf(FriendlyByteBuf buf) {
        String string = buf.readUtf();
        JsonElement compressed = GSON.fromJson(string, JsonElement.class);
        JsonElement json = COMPRESSOR.decompress(compressed);
        return Metadata.fromJson(json.getAsJsonObject());
    }

    public record Metadata(RollbackWorld world, RollbackDefaultConfig config) {
        public static Metadata fromJson(JsonObject json) {
            JsonElement worldJson = json.get("world");
            JsonElement configJson = json.get("defaultConfig");
            RollbackWorld world = BackupManager.GSON.fromJson(worldJson, RollbackWorld.class);
            RollbackDefaultConfig config = RollbackDefaultConfig.GSON.fromJson(configJson, RollbackDefaultConfig.class);
            return new Metadata(world, config);
        }

        public JsonObject toJson() {
            JsonElement worldJson = BackupManager.GSON.toJsonTree(this.world, RollbackWorld.class);
            JsonElement configJson = this.config.getGson().toJsonTree(this.config);
            JsonObject json = new JsonObject();
            json.add("world", worldJson);
            json.add("defaultConfig", configJson);
            return json;
        }
    }
}
