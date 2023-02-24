package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.util.mixin.WorldEntryExpanded;
import ir.mehradn.rollback.util.mixin.PublicStatics;
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

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow private ButtonWidget recreateButton;
    @Shadow private WorldListWidget levelList;

    @Shadow private ButtonWidget selectButton;
    private ButtonWidget rollbackButton;
    private int[] buttonPosition;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private int hideButton(int x, int y, int width, int height) {
        buttonPosition = new int[]{x, y, width, height};
        return -99999;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 5, target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private void addButton(CallbackInfo ci) {
        this.rollbackButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.button"),
                (button) -> this.levelList.getSelectedAsOptional().ifPresent((worldEntry) -> ((WorldEntryExpanded)(Object)worldEntry).rollback())
        ).dimensions(buttonPosition[0], buttonPosition[1], buttonPosition[2], buttonPosition[3]).build());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void changeScreen(CallbackInfo ci) {
        if (PublicStatics.playWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.playWorld)) {
                this.levelList.setSelected(world);
                this.selectButton.onPress();
            }
        } else if (PublicStatics.recreateWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.recreateWorld)) {
                this.levelList.setSelected(world);
                this.recreateButton.onPress();
            }
        } else if (PublicStatics.rollbackWorld != null) {
            try (WorldListWidget.WorldEntry world = this.levelList.new WorldEntry(this.levelList, PublicStatics.rollbackWorld)) {
                this.levelList.setSelected(world);
                this.rollbackButton.onPress();
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
