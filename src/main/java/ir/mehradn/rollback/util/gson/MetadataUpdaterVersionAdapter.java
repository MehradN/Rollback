package ir.mehradn.rollback.util.gson;

import com.google.gson.*;
import ir.mehradn.rollback.util.backup.MetadataUpdater;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.lang.reflect.Type;

@Environment(EnvType.CLIENT)
public class MetadataUpdaterVersionAdapter implements JsonSerializer<MetadataUpdater.Version>, JsonDeserializer<MetadataUpdater.Version> {
    public static final MetadataUpdaterVersionAdapter INSTANCE = new MetadataUpdaterVersionAdapter();

    public MetadataUpdater.Version deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return MetadataUpdater.Version.fromString(json.getAsString());
    }

    public JsonElement serialize(MetadataUpdater.Version obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.toString());
    }
}