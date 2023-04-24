package ir.mehradn.rollback.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.mixin.WorldListEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow private WorldSelectionList list;
    private Button rollbackButton;
    private int[] buttonPos;

    protected SelectWorldScreenMixin(Component component) {
        super(component);
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"))
    private Button hideButton(Button btn) {
        if (Component.translatable("selectWorld.recreate").equals(btn.getMessage())) {
            this.buttonPos = new int[]{ btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight() };
            if (RollbackConfig.replaceReCreateButton)
                btn.setY(-99999);
            else
                this.buttonPos[1] = -99999;
        }
        return btn;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screens/worldselection/SelectWorldScreen;updateButtonStatus(ZZ)V"))
    private void addButton(CallbackInfo ci) {
        if (RollbackConfig.replaceReCreateButton) {
            this.rollbackButton = addRenderableWidget(Button.builder(
                Component.translatable("rollback.button"),
                (button) -> this.list.getSelectedOpt().ifPresent((worldEntry) -> ((WorldListEntryExpanded)(Object)worldEntry).rollbackWorld())
            ).bounds(this.buttonPos[0], this.buttonPos[1], this.buttonPos[2], this.buttonPos[3]).build());
        }
    }

    @Inject(method = "updateButtonStatus", at = @At("RETURN"))
    private void onUpdateButtonStatus(boolean isPlayable, boolean isSelectable, CallbackInfo ci) {
        if (RollbackConfig.replaceReCreateButton)
            this.rollbackButton.active = isPlayable;
    }
}
