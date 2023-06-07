package ir.mehradn.rollback.config;

import com.google.gson.JsonObject;
import ir.mehradn.rollback.Rollback;

public class ConfigUpdater {
    private final JsonObject json;

    public ConfigUpdater(JsonObject json) {
        this.json = json;
    }

    public void update() {
        Rollback.LOGGER.info("Updating the config...");
        if (this.json.has("backupsPerWorld")) {
            int value = this.json.get("backupsPerWorld").getAsInt();
            this.json.remove("backupsPerWorld");
            this.json.addProperty("maxBackups", value);
        }
        if (this.json.has("replaceReCreateButton")) {
            boolean value = this.json.get("replaceReCreateButton").getAsBoolean();
            this.json.remove("replaceReCreateButton");
            this.json.addProperty("replaceButton", value);
        }
        if (this.json.has("commandAccess") && this.json.getAsJsonPrimitive("commandAccess").isString()) {
            String value = this.json.get("commandAccess").getAsString();
            boolean newValue = value.equals("ALWAYS");
            this.json.remove("commandAccess");
            this.json.addProperty("commandAccess", newValue);
        }
        if (this.json.has("replaceGameRulesButton")) {
            this.json.remove("replaceGameRulesButton");
        }
    }
}
