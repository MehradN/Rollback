package ir.mehradn.rollback.mixin;

import com.mojang.serialization.Lifecycle;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow private Button gameRulesButton;

    private CycleButton<Boolean> automatedButton;
    private int[] buttonPos;
    private boolean promptEnabled = true;
    private boolean enablePrompted = false;

    protected CreateWorldScreenMixin(Component component) {
        super(component);
    }

    @Shadow protected abstract void createNewWorld(PrimaryLevelData.SpecialWorldProperty specialWorldProperty, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, Lifecycle worldGenSettingsLifecycle);

    @ModifyArg(method = "init", index = 0, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener changeButton(GuiEventListener widget) {
        Button button = (Button)widget;

        if (RollbackConfig.replaceGameRulesButton()) {
            this.buttonPos = new int[]{button.getX(), button.getY()};
            button.setPosition(this.width / 2 + 5, 151);
        } else {
            this.buttonPos = new int[]{this.width / 2 + 5, 151};
        }
        return button;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screens/worldselection/WorldGenSettingsComponent;init(Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/Font;)V"))
    private void addMyButton(CallbackInfo ci) {
        this.automatedButton = addRenderableWidget(CycleButton.onOffBuilder(false).create(
            this.buttonPos[0], this.buttonPos[1], 150, 20,
            Component.translatable("rollback.screen.automatedOption")
        ));

        if (RollbackConfig.replaceGameRulesButton())
            this.gameRulesButton.visible = false;
        else
            this.automatedButton.visible = false;
    }

    @Inject(method = "setWorldGenSettingsVisible", at = @At("RETURN"))
    private void toggleButtons(boolean moreOptionsOpen, CallbackInfo ci) {
        boolean f = RollbackConfig.replaceGameRulesButton() ^ moreOptionsOpen;
        this.gameRulesButton.visible = !f;
        this.automatedButton.visible = f;
    }

    @Inject(method = "createNewWorld", cancellable = true, at = @At("HEAD"))
    private void promptFeature(PrimaryLevelData.SpecialWorldProperty specialWorldProperty,
                               LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
                               Lifecycle worldGenSettingsLifecycle,
                               CallbackInfo ci) {
        if (RollbackConfig.promptDisabled()) {
            this.enablePrompted = false;
            return;
        }
        if (!this.promptEnabled) {
            this.promptEnabled = true;
            return;
        }
        if (!this.automatedButton.getValue()) {
            this.minecraft.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    this.automatedButton.setValue(confirmed);
                    this.promptEnabled = false;
                    this.enablePrompted = true;
                    this.createNewWorld(specialWorldProperty, layeredRegistryAccess, worldGenSettingsLifecycle);
                },
                Component.translatable("rollback.screen.enableAutomatedQuestion"),
                Component.empty(),
                Component.translatable("gui.yes"),
                Component.translatable("gui.no")
            ));
            ci.cancel();
        }
        this.enablePrompted = false;
    }

    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    private void saveOption(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> ci) {
        if (ci.getReturnValue().isEmpty())
            return;
        String worldName = ci.getReturnValue().get().getLevelId();
        BackupManager backupManager = new BackupManager();
        backupManager.setAutomated(worldName, this.automatedButton.getValue());
        if (this.enablePrompted) {
            backupManager.setPrompted(worldName);
            this.enablePrompted = false;
        }
    }
}
