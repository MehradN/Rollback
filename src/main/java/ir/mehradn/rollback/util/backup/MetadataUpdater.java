package ir.mehradn.rollback.util.backup;

import com.google.gson.JsonObject;
import ir.mehradn.rollback.Rollback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MetadataUpdater {
    private final JsonObject metadata;

    public MetadataUpdater(JsonObject metadata) {
        this.metadata = metadata;
    }

    public Version getVersion() {
        if (this.metadata.has("version") && this.metadata.get("version").isJsonPrimitive())
            return Version.fromString(this.metadata.get("version").getAsString());
        else
            return Version.DEFAULT_VERSION;
    }

    public JsonObject update() {
        Rollback.LOGGER.info("Updating the metadata...");
        Version version = getVersion();
        if (version.isLessThan(0, 3))
            V0_3();
        return this.metadata;
    }

    private void V0_3() {
        Rollback.LOGGER.info("Updating to V0.3...");
        JsonObject worldsData = new JsonObject();
        for (String key : this.metadata.keySet()) {
            JsonObject worldObject = new JsonObject();
            worldObject.addProperty("automated", true);
            worldObject.add("backups", this.metadata.getAsJsonArray(key));
            worldsData.add(key, worldObject);
            this.metadata.remove(key);
        }
        this.metadata.addProperty("version", "0.3");
        this.metadata.add("worlds", worldsData);
    }

    @Environment(EnvType.CLIENT)
    public static final class Version {
        public static final Version LATEST_VERSION = new Version(0, 3);
        public static final Version DEFAULT_VERSION = new Version(0, 1);
        private final int major;
        private final int minor;

        public Version(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        public static Version fromString(String version) {
            String[] versionParts = version.split("\\.");
            return new Version(
                Integer.parseInt(versionParts[0]),
                Integer.parseInt(versionParts[1])
            );
        }

        public boolean isLessThan(int major, int minor) {
            return (this.major < major || (this.major == major && this.minor < minor));
        }

        public boolean isLessThan(Version version) {
            return isLessThan(version.major, version.minor);
        }

        public boolean isOutdated() {
            return isLessThan(LATEST_VERSION);
        }

        public String toString() {
            return this.major + "." + this.minor;
        }
    }
}
