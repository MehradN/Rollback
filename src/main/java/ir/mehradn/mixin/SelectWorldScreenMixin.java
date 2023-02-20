package ir.mehradn.mixin;

import ir.mehradn.gui.RollbackScreen;
import ir.mehradn.util.PublicStatics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow private ButtonWidget recreateButton;
    @Shadow private WorldListWidget levelList;

    private ButtonWidget rollbackButton;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;worldSelected(Z)V", ordinal = 0))
    private void addButton(CallbackInfo ci) {
        this.recreateButton.visible = false;
        this.recreateButton.active = false;
        this.rollbackButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.selectWorld.button"), (button) -> {
            Optional<WorldListWidget.WorldEntry> w = this.levelList.getSelectedAsOptional();
            w.ifPresent(entry -> this.client.setScreen(new RollbackScreen(this, ((WorldEntryAccessor)(Object)entry).getLevel())));
        }).dimensions(this.recreateButton.getX(), this.recreateButton.getY(), this.recreateButton.getWidth(), this.recreateButton.getHeight()).build());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void changeScreen(CallbackInfo ci) {
        if (PublicStatics.recreateWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.recreateWorld)) {
                this.levelList.setSelected(world);
                this.recreateButton.onPress();
            }
            finally {
                PublicStatics.recreateWorld = null;
            }
        } else if (PublicStatics.rollbackWorld != null) {
            this.client.setScreen(new RollbackScreen(this, PublicStatics.rollbackWorld));
            PublicStatics.rollbackWorld = null;
        }
    }

    @Inject(method = "worldSelected", at = @At("RETURN"))
    private void onWorldSelected(boolean active, CallbackInfo ci) {
        this.recreateButton.active = false;
        this.rollbackButton.active = active;
    }
}
