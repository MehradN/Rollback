package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.annotations.SerializedName;
import ir.mehradn.rollback.rollback.BackupManager;
import java.util.HashMap;
import java.util.Map;

public class RollbackData implements RollbackMetadata {
    @SerializedName("version") public RollbackVersion version = RollbackVersion.LATEST_VERSION;
    @SerializedName("worlds") public Map<String, RollbackWorld> worlds = new HashMap<>();

    public RollbackWorld getWorld(String levelID) {
        if (!this.worlds.containsKey(levelID))
            this.worlds.put(levelID, new RollbackWorld());
        return this.worlds.get(levelID);
    }

    @Override
    public void update(BackupManager backupManager) {
        for (RollbackWorld world : this.worlds.values())
            world.update(backupManager);
    }
}
