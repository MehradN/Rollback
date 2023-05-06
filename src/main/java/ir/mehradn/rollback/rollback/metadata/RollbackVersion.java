package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.*;
import java.lang.reflect.Type;

public class RollbackVersion {
    public static final RollbackVersion LATEST_VERSION = new RollbackVersion(0, 9);
    public static final RollbackVersion DEFAULT_VERSION = new RollbackVersion(0, 1);
    private final int major;
    private final int minor;

    public RollbackVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static RollbackVersion fromString(String version) {
        String[] versionParts = version.split("\\.");
        return new RollbackVersion(
            Integer.parseInt(versionParts[0]),
            Integer.parseInt(versionParts[1])
        );
    }

    public boolean isLessThan(int major, int minor) {
        return (this.major < major || (this.major == major && this.minor < minor));
    }

    public boolean isLessThan(RollbackVersion version) {
        return isLessThan(version.major, version.minor);
    }

    public boolean isOutdated() {
        return isLessThan(LATEST_VERSION);
    }

    public String toString() {
        return this.major + "." + this.minor;
    }

    public static class Adapter implements JsonSerializer<RollbackVersion>, JsonDeserializer<RollbackVersion> {
        public RollbackVersion deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return fromString(json.getAsString());
        }

        public JsonElement serialize(RollbackVersion obj, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(obj.toString());
        }
    }
}
