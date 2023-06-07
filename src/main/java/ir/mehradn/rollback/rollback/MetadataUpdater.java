package ir.mehradn.rollback.rollback;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;

public class MetadataUpdater {
    private final JsonObject json;

    public MetadataUpdater(JsonObject json) {
        this.json = json;
    }

    public RollbackVersion getVersion() {
        if (this.json.has("version") && this.json.get("version").isJsonPrimitive())
            return RollbackVersion.fromString(this.json.get("version").getAsString());
        else
            return RollbackVersion.DEFAULT_VERSION;
    }

    public void update() {
        Rollback.LOGGER.info("Updating the metadata...");
        RollbackVersion version = getVersion();
        if (version.isLessThan(0, 3))
            V0_3();
        if (version.isLessThan(0, 9))
            V0_9();
    }

    private void V0_3() {
        Rollback.LOGGER.info("Updating to V0.3...");
        JsonObject worldsData = new JsonObject();
        for (String key : this.json.keySet()) {
            JsonObject worldObject = new JsonObject();
            worldObject.addProperty("automated", true);
            worldObject.add("backups", this.json.getAsJsonArray(key));
            worldsData.add(key, worldObject);
            this.json.remove(key);
        }
        this.json.addProperty("version", "0.3");
        this.json.add("worlds", worldsData);
    }

    private void V0_9() {
        Rollback.LOGGER.info("Updating to V0.9...");
        JsonObject worlds = this.json.getAsJsonObject("worlds");
        for (String levelID : worlds.keySet()) {
            JsonObject world = worlds.getAsJsonObject(levelID);

            if (world.has("automated")) {
                boolean value = world.getAsJsonPrimitive("automated").getAsBoolean();
                world.remove("automated");
                JsonObject config = new JsonObject();
                config.addProperty("backupEnabled", value);
                world.add("config", config);
            }

            JsonArray oldBackups = world.getAsJsonArray("backups");
            JsonObject newBackups = new JsonObject();
            for (int i = 0; i < world.size(); i++)
                newBackups.add(String.valueOf(i + 1), oldBackups.get(i));
            world.remove("backups");
            world.add("rollbacks", newBackups);
        }
    }
}
