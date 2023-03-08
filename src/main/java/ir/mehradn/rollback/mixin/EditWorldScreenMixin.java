package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.EditWorldScreenExpanded;
import ir.mehradn.rollback.util.mixin.WorldSelectionListCallbackAction;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen implements EditWorldScreenExpanded {
    @Shadow @Final private BooleanConsumer callback;

    private Consumer<WorldSelectionListCallbackAction> callbackActionConsumer = null;
    private int[] buttonPos1;
    private int[] buttonPos2;

    protected EditWorldScreenMixin(Component component) {
        super(component);
    }

    public void setCallbackAction(Consumer<WorldSelectionListCallbackAction> consumer) {
        this.callbackActionConsumer = consumer;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/EditWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener hideButtons(GuiEventListener elm) {
        if (!(elm instanceof Button btn))
            return elm;
        if (Component.translatable("selectWorld.edit.backup").equals(btn.getMessage())) {
            this.buttonPos1 = new int[]{btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight()};
            btn.setY(-99999);
        } else if (Component.translatable("selectWorld.edit.backupFolder").equals(btn.getMessage())) {
            this.buttonPos2 = new int[]{btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight()};
            btn.setY(-99999);
        }
        return btn;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButtons(CallbackInfo ci) {
        addRenderableWidget(Button.builder(
            Component.translatable("rollback.editWorld.button"),
            (button) -> {
                if (this.callbackActionConsumer != null)
                    this.callbackActionConsumer.accept(WorldSelectionListCallbackAction.ROLLBACK_WORLD);
                else
                    this.callback.accept(false);
            }
        ).bounds(this.buttonPos1[0], this.buttonPos1[1], this.buttonPos1[2], this.buttonPos1[3]).build());

        addRenderableWidget(Button.builder(
            Component.translatable("rollback.editWorld.recreateButton"),
            (button) -> {
                if (this.callbackActionConsumer != null)
                    this.callbackActionConsumer.accept(WorldSelectionListCallbackAction.RECREATE_WORLD);
                else
                    this.callback.accept(false);
            }
        ).bounds(this.buttonPos2[0], this.buttonPos2[1], this.buttonPos2[2], this.buttonPos2[3]).build());
    }
}
