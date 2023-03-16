package ir.mehradn.rollback.util.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface CreateWorldScreenExpanded {
    boolean getAutomatedBackups();

    void setAutomatedBackups(boolean enabled);
}
