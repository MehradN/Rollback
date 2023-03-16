package ir.mehradn.rollback.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.CreateWorldScreenExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen implements CreateWorldScreenExpanded {
    private boolean automatedBackups = false;

    protected CreateWorldScreenMixin(Component component) {
        super(component);
    }

    public boolean getAutomatedBackups() {
        return this.automatedBackups;
    }

    public void setAutomatedBackups(boolean enabled) {
        this.automatedBackups = enabled;
    }

    @ModifyReturnValue(method = "createNewWorldDirectory", at = @At("RETURN"))
    private Optional<LevelStorageSource.LevelStorageAccess> saveOption(Optional<LevelStorageSource.LevelStorageAccess> optional) {
        if (optional.isPresent()) {
            String worldName = optional.get().getLevelId();
            BackupManager backupManager = new BackupManager();
            backupManager.setAutomated(worldName, getAutomatedBackups());
        }
        return optional;
    }
}
