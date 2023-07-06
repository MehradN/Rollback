package ir.mehradn.rollback.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackClientConfig;
import ir.mehradn.rollback.util.mixin.WorldListEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    private final RollbackClientConfig config = new RollbackClientConfig();
    @Shadow private WorldSelectionList list;
    private ScreenRectangle buttonBounds;
    private Button rollbackButton;

    protected SelectWorldScreenMixin(Component component) {
        super(component);
    }

    @Override
    public void added() {
        Rollback.LOGGER.info("Loading config for select world screen...");
        try {
            this.config.load();
        } catch (IOException e) {
            Rollback.LOGGER.error("Failed to load config for select world screen!", e);
            this.config.reset();
        }
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"))
    private Button hideButton(Button button) {
        if (button.getMessage().equals(Component.translatable("selectWorld.recreate"))) {
            int x = button.getX(), y = button.getY(), w = button.getWidth(), h = button.getHeight();
            if (this.config.replaceButton.get())
                button.setY(-9999);
            else
                y = -9999;
            this.buttonBounds = new ScreenRectangle(x, y, w, h);
        }
        return button;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screens/worldselection/SelectWorldScreen;updateButtonStatus(ZZ)V"))
    private void addButton(CallbackInfo ci) {
        if (this.config.replaceButton.get()) {
            this.rollbackButton = addRenderableWidget(Button.builder(
                Component.translatable("rollback.mixin.button.rollback"),
                (button) -> this.list.getSelectedOpt().ifPresent((worldEntry) -> ((WorldListEntryExpanded)(Object)worldEntry).rollbackWorld())
            ).bounds(this.buttonBounds.left(), this.buttonBounds.top(), this.buttonBounds.width(), this.buttonBounds.height()).build());
        }
    }

    @Inject(method = "updateButtonStatus", at = @At("RETURN"))
    private void onUpdateButtonStatus(boolean isPlayable, boolean isSelectable, CallbackInfo ci) {
        if (this.config.replaceButton.get())
            this.rollbackButton.active = isSelectable;
    }
}
