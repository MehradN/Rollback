package ir.mehradn.rollback;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

@Environment(EnvType.CLIENT)
public class RollbackPreLaunch implements PreLaunchEntrypoint {
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
    }
}
