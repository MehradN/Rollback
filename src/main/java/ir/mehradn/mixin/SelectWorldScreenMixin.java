package ir.mehradn.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    private ButtonWidget recreateButton;

    private ButtonWidget rollbackButton;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;worldSelected(Z)V", ordinal = 0))
    private void onInit(CallbackInfo ci) {
        this.recreateButton.visible = false;
        this.recreateButton.active = false;
        this.rollbackButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.button"), (button) -> {
            System.out.println("BUTTON PRESSED!!!");
        }).dimensions(this.recreateButton.getX(), this.recreateButton.getY(), this.recreateButton.getWidth(), this.recreateButton.getHeight()).build());
    }

    @Inject(method = "worldSelected", at = @At("RETURN"))
    private void onWorldSelected(boolean active, CallbackInfo ci) {
        this.recreateButton.active = false;
        this.rollbackButton.active = active;
    }
}
