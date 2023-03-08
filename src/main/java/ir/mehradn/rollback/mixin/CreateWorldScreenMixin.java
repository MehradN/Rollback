package ir.mehradn.rollback.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Lifecycle;
import ir.mehradn.rollback.util.backup.BackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    private boolean redirectCreateNewWorld = true;
    private boolean promptAnswer = true;

    protected CreateWorldScreenMixin(Component component) {
        super(component);
    }

    @Shadow protected abstract void createNewWorld(PrimaryLevelData.SpecialWorldProperty specialWorldProperty, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, Lifecycle worldGenSettingsLifecycle);

    @Inject(method = "createNewWorld", cancellable = true, at = @At("HEAD"))
    private void promptFeature(PrimaryLevelData.SpecialWorldProperty specialWorldProperty,
                               LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
                               Lifecycle worldGenSettingsLifecycle,
                               CallbackInfo ci) {
        if (!this.redirectCreateNewWorld) {
            this.redirectCreateNewWorld = true;
            return;
        }
        this.minecraft.setScreen(new ConfirmScreen(
            (confirmed) -> {
                this.redirectCreateNewWorld = false;
                this.promptAnswer = confirmed;
                this.createNewWorld(specialWorldProperty, layeredRegistryAccess, worldGenSettingsLifecycle);
            },
            Component.translatable("rollback.screen.enableAutomatedQuestion"),
            Component.empty(),
            Component.translatable("gui.yes"),
            Component.translatable("gui.no")
        ));
        ci.cancel();
    }

    @ModifyReturnValue(method = "createNewWorldDirectory", at = @At("RETURN"))
    private Optional<LevelStorageSource.LevelStorageAccess> saveOption(Optional<LevelStorageSource.LevelStorageAccess> optional) {
        if (optional.isPresent()) {
            String worldName = optional.get().getLevelId();
            BackupManager backupManager = new BackupManager();
            backupManager.setAutomated(worldName, this.promptAnswer);
        }
        return optional;
    }
}
