package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.PublicStatics;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final
    private BooleanConsumer callback;
    @Shadow @Final
    private LevelStorage.Session storageSession;

    private int[] buttonPos1;
    private int[] buttonPos2;

    protected EditWorldScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private int hideButton1(int x, int y, int width, int height) {
        this.buttonPos1 = new int[]{x, y, width, height};
        return -99999;
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private int hideButton2(int x, int y, int width, int height) {
        this.buttonPos2 = new int[]{x, y, width, height};
        return -99999;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/screen/world/EditWorldScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private void addButtons(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.editWorld.button"),
            (button) -> {
                PublicStatics.playWorld = null;
                PublicStatics.recreateWorld = null;
                PublicStatics.rollbackWorld = this.storageSession.getLevelSummary();
                this.callback.accept(false);
            }
        ).dimensions(this.buttonPos1[0], this.buttonPos1[1], this.buttonPos1[2], this.buttonPos1[3]).build());

        addDrawableChild(ButtonWidget.builder(
            Text.translatable("rollback.editWorld.recreateButton"),
            (button) -> {
                PublicStatics.playWorld = null;
                PublicStatics.recreateWorld = this.storageSession.getLevelSummary();
                PublicStatics.rollbackWorld = null;
                this.callback.accept(false);
            }
        ).dimensions(this.buttonPos2[0], this.buttonPos2[1], this.buttonPos2[2], this.buttonPos2[3]).build());
    }
}
