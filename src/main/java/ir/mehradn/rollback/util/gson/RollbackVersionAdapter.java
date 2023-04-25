package ir.mehradn.rollback.util.gson;

import com.google.gson.*;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import java.lang.reflect.Type;

public class RollbackVersionAdapter implements JsonSerializer<RollbackVersion>, JsonDeserializer<RollbackVersion> {
    public RollbackVersion deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return RollbackVersion.fromString(json.getAsString());
    }
    public JsonElement serialize(RollbackVersion obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.toString());
    }
}
