package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.mixin.PublicStatics;
import ir.mehradn.rollback.util.mixin.WorldEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    private WorldListWidget levelList;

    private ButtonWidget rollbackButton;
    private int[] buttonPos;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private int hideButton(int x, int y, int width, int height) {
        this.buttonPos = new int[]{x, y, width, height};
        if (RollbackConfig.replaceReCreateButton())
            return -99999;
        this.buttonPos[1] = -99999;
        return y;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 5, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private void addButton(CallbackInfo ci) {
        if (RollbackConfig.replaceReCreateButton()) {
            this.rollbackButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("rollback.button"),
                (button) -> this.levelList.getSelectedAsOptional().ifPresent((worldEntry) -> ((WorldEntryExpanded)(Object)worldEntry).rollback())
            ).dimensions(this.buttonPos[0], this.buttonPos[1], this.buttonPos[2], this.buttonPos[3]).build());
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void changeScreen(CallbackInfo ci) {
        if (PublicStatics.playWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.playWorld)) {
                world.play();
            }
        } else if (PublicStatics.recreateWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.recreateWorld)) {
                world.recreate();
            }
        } else if (PublicStatics.rollbackWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.rollbackWorld)) {
                ((WorldEntryExpanded)(Object)world).rollback();
            }
        }
        PublicStatics.playWorld = null;
        PublicStatics.recreateWorld = null;
        PublicStatics.rollbackWorld = null;
    }

    @Inject(method = "worldSelected", at = @At("RETURN"))
    private void onWorldSelected(boolean active, CallbackInfo ci) {
        this.rollbackButton.active = active;
    }
}
