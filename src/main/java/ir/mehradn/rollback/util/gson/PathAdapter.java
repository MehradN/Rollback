package ir.mehradn.rollback.util.gson;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.lang.reflect.Type;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class PathAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {
    public static final PathAdapter INSTANCE = new PathAdapter();

    public Path deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Path.of(json.getAsString());
    }

    public JsonElement serialize(Path obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.toString());
    }
}
