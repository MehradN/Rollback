package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;
import java.lang.reflect.Type;

public class RollbackVersion implements RollbackMetadata {
    public static final RollbackVersion LATEST_VERSION = new RollbackVersion((short)0, (short)9);
    public static final RollbackVersion DEFAULT_VERSION = new RollbackVersion((short)0, (short)1);
    public final short major;
    public final short minor;

    public RollbackVersion(short major, short minor) {
        this.major = major;
        this.minor = minor;
    }

    public static RollbackVersion fromString(String version) {
        String[] versionParts = version.split("\\.");
        return new RollbackVersion(
            Short.parseShort(versionParts[0]),
            Short.parseShort(versionParts[1])
        );
    }

    public static RollbackVersion fromBuf(FriendlyByteBuf buf) {
        return new RollbackVersion(buf.readShort(), buf.readShort());
    }

    public boolean isLessThan(int major, int minor) {
        return (this.major < major || (this.major == major && this.minor < minor));
    }

    public boolean isLessThan(RollbackVersion version) {
        return isLessThan(version.major, version.minor);
    }

    public boolean equals(int major, int minor) {
        return (this.major == major && this.minor == minor);
    }

    public boolean equals(RollbackVersion version) {
        return equals(version.major, version.minor);
    }

    public boolean isOutdated() {
        return isLessThan(LATEST_VERSION);
    }

    public boolean notMatch() {
        return !equals(LATEST_VERSION);
    }

    public String toString() {
        return this.major + "." + this.minor;
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        buf.writeShort(this.major);
        buf.writeShort(this.minor);
    }

    public static class Adapter implements JsonSerializer<RollbackVersion>, JsonDeserializer<RollbackVersion> {
        @Override
        public RollbackVersion deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(RollbackVersion obj, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(obj.toString());
        }
    }
}
