package ir.mehradn.rollback.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.Map;

public final class JsonCompressor {
    public static final String IGNORED_KEY = "__ignored__";
    private static final HashMap<String, String> COMPRESSION;
    private static final HashMap<String, String> DECOMPRESSION;
    private final boolean integrated;

    static {
        COMPRESSION = new HashMap<>();
        DECOMPRESSION = new HashMap<>();

        addCompression("backup_file", "a");
        addCompression("icon_file", "b");
        addCompression("creation_date", "c");
        addCompression("days_played", "d");
        addCompression("file_size", "e");
        addCompression("name", "f");
        addCompression("backupEnabled", "g");
        addCompression("maxBackups", "h");
        addCompression("backupFrequency", "i");
        addCompression("timerMode", "j");
        addCompression("prompted", "k");
        addCompression("days_passed", "l");
        addCompression("since_day", "m");
        addCompression("since_backup", "n");
        addCompression("last_id", "o");
        addCompression("config", "p");
        addCompression("rollbacks", "q");
        addCompression("backups", "r");
        addCompression("world", "s");
        addCompression("defaultConfig", "t");
        addCompression("DAYLIGHT_CYCLE", "u");
        addCompression("IN_GAME_TIME", "v");
    }

    public JsonCompressor(boolean integrated) {
        this.integrated = integrated;
    }

    public JsonElement compress(JsonElement json) {
        return compress(IGNORED_KEY, json);
    }

    public JsonElement decompress(JsonElement json) {
        return decompress(IGNORED_KEY, json);
    }

    private static void addCompression(String decompressed, String compressed) {
        COMPRESSION.put(decompressed, compressed);
        DECOMPRESSION.put(compressed, decompressed);
    }

    private JsonPrimitive compress(String key, JsonPrimitive value) {
        if (value.isString())
            return new JsonPrimitive(compressString(key, value.getAsString()));
        return value.deepCopy();
    }

    private JsonPrimitive decompress(String key, JsonPrimitive value) {
        if (value.isString())
            return new JsonPrimitive(decompressString(key, value.getAsString()));
        return value.deepCopy();
    }

    private JsonArray compress(JsonArray value) {
        JsonArray array = new JsonArray();
        for (JsonElement element : value)
            array.add(compress(IGNORED_KEY, element));
        return array;
    }

    private JsonArray decompress(JsonArray value) {
        JsonArray array = new JsonArray();
        for (JsonElement element : value)
            array.add(decompress(IGNORED_KEY, element));
        return array;
    }

    private JsonObject compress(JsonObject value) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
            String key = compressKey(entry.getKey());
            JsonElement val = compress(entry.getKey(), entry.getValue());
            object.add(key, val);
        }
        return object;
    }

    private JsonObject decompress(JsonObject value) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
            String key = decompressKey(entry.getKey());
            JsonElement val = decompress(key, entry.getValue());
            object.add(key, val);
        }
        return object;
    }

    private JsonElement compress(String key, JsonElement value) {
        if (value.isJsonPrimitive())
            return compress(key, value.getAsJsonPrimitive());
        if (value.isJsonArray())
            return compress(value.getAsJsonArray());
        if (value.isJsonObject())
            return compress(value.getAsJsonObject());
        return value.deepCopy();
    }

    private JsonElement decompress(String key, JsonElement value) {
        if (value.isJsonPrimitive())
            return decompress(key, value.getAsJsonPrimitive());
        if (value.isJsonArray())
            return decompress(value.getAsJsonArray());
        if (value.isJsonObject())
            return decompress(value.getAsJsonObject());
        return value.deepCopy();
    }

    private String compressKey(String key) {
        if (COMPRESSION.containsKey(key))
            return COMPRESSION.get(key);
        return key;
    }

    private String decompressKey(String key) {
        if (DECOMPRESSION.containsKey(key))
            return DECOMPRESSION.get(key);
        return key;
    }

    private String compressString(String key, String value) {
        if (key.equals("backup_file"))
            return "";
        if (key.equals("icon_file"))
            return (this.integrated && value.length() <= 64 ? value : "");
        if (key.equals("timerMode"))
            return compressKey(value);
        return value;
    }

    private String decompressString(String key, String value) {
        if (key.equals("timerMode"))
            return decompressKey(value);
        return value;
    }
}
