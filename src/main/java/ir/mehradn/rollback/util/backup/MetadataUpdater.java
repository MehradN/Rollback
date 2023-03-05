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

    public static Version getVersion(JsonObject metadata) {
        if (metadata.has("version") && metadata.get("version").isJsonPrimitive())
            return new Version(metadata.get("version").getAsString());
        else
            return new Version(0, 1);
    }

    public JsonObject update() {
        Rollback.LOGGER.info("Updating the metadata...");
        Version version = getVersion(this.metadata);
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
        private final int major;
        private final int minor;

        public Version(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        public Version(String version) {
            String[] versionParts = version.split("\\.");
            this.major = Integer.parseInt(versionParts[0]);
            this.minor = Integer.parseInt(versionParts[1]);
        }

        public boolean isLessThan(int major, int minor) {
            return (this.major < major || (this.major == major && this.minor < minor));
        }
    }
}
