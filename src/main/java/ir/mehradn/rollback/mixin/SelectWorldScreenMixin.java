package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.mixin.PublicStatics;
import ir.mehradn.rollback.util.mixin.WorldListEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"))
    private int hideButton(int x, int y, int width, int height) {
        this.buttonPos = new int[]{x, y, width, height};
        if (RollbackConfig.replaceReCreateButton())
            return -99999;
        this.buttonPos[1] = -99999;
        return y;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 5, target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"))
    private void addButton(CallbackInfo ci) {
        if (RollbackConfig.replaceReCreateButton()) {
            this.rollbackButton = addRenderableWidget(Button.builder(
                Component.translatable("rollback.button"),
                (button) -> this.list.getSelectedOpt().ifPresent((worldEntry) -> ((WorldListEntryExpanded)(Object)worldEntry).rollbackWorld())
            ).bounds(this.buttonPos[0], this.buttonPos[1], this.buttonPos[2], this.buttonPos[3]).build());
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void changeScreen(CallbackInfo ci) {
        if (PublicStatics.joinWorld != null) {
            try (WorldSelectionList.WorldListEntry world = this.list.new WorldListEntry(this.list, PublicStatics.joinWorld)) {
                world.joinWorld();
            }
        } else if (PublicStatics.recreateWorld != null) {
            try (WorldSelectionList.WorldListEntry world = this.list.new WorldListEntry(this.list, PublicStatics.recreateWorld)) {
                world.recreateWorld();
            }
        } else if (PublicStatics.rollbackWorld != null) {
            try (WorldSelectionList.WorldListEntry world = this.list.new WorldListEntry(this.list, PublicStatics.rollbackWorld)) {
                ((WorldListEntryExpanded)(Object)world).rollbackWorld();
            }
        }
        PublicStatics.joinWorld = null;
        PublicStatics.recreateWorld = null;
        PublicStatics.rollbackWorld = null;
    }

    @Inject(method = "updateButtonStatus", at = @At("RETURN"))
    private void onUpdateButtonStatus(boolean active, CallbackInfo ci) {
        this.rollbackButton.active = active;
    }
}
