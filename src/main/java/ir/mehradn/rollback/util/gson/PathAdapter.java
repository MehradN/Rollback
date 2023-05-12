package ir.mehradn.rollback.util.gson;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.nio.file.Path;

public class PathAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {
    @Override
    public Path deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Path.of(json.getAsString());
    }

    @Override
    public JsonElement serialize(Path obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.toString());
    }
}
