package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.PublicStatics;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final private BooleanConsumer callback;
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;

    private int[] buttonPos1;
    private int[] buttonPos2;

    protected EditWorldScreenMixin(Component component) {
        super(component);
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"))
    private int hideButton1(int x, int y, int width, int height) {
        this.buttonPos1 = new int[]{x, y, width, height};
        return -99999;
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"))
    private int hideButton2(int x, int y, int width, int height) {
        this.buttonPos2 = new int[]{x, y, width, height};
        return -99999;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/screens/worldselection/EditWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private void addButtons(CallbackInfo ci) {
        addRenderableWidget(Button.builder(
            Component.translatable("rollback.editWorld.button"),
            (button) -> {
                PublicStatics.joinWorld = null;
                PublicStatics.recreateWorld = null;
                PublicStatics.rollbackWorld = this.levelAccess.getSummary();
                this.callback.accept(false);
            }
        ).bounds(this.buttonPos1[0], this.buttonPos1[1], this.buttonPos1[2], this.buttonPos1[3]).build());

        addRenderableWidget(Button.builder(
            Component.translatable("rollback.editWorld.recreateButton"),
            (button) -> {
                PublicStatics.joinWorld = null;
                PublicStatics.recreateWorld = this.levelAccess.getSummary();
                PublicStatics.rollbackWorld = null;
                this.callback.accept(false);
            }
        ).bounds(this.buttonPos2[0], this.buttonPos2[1], this.buttonPos2[2], this.buttonPos2[3]).build());
    }
}
