package ir.mehradn.rollback.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import ir.mehradn.rollback.util.mixin.CreateWorldScreenExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net/minecraft/client/gui/screens/worldselection/CreateWorldScreen$GameTab")
public abstract class GameTabMixin extends GridLayoutTab {
    private GridLayout.RowHelper grabbedRowHelper;

    public GameTabMixin(Component component) {
        super(component);
    }

    @ModifyReceiver(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;Lnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private GridLayout.RowHelper grabRowHelper(GridLayout.RowHelper rowHelper, LayoutElement layoutElement, LayoutSettings layoutSettings) {
        this.grabbedRowHelper = rowHelper;
        return rowHelper;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addAutomatedBackupsButton(CreateWorldScreen createWorldScreen, CallbackInfo ci) {
        CreateWorldScreenExpanded expanded = (CreateWorldScreenExpanded)createWorldScreen;
        this.grabbedRowHelper.addChild(CycleButton.onOffBuilder(expanded.getAutomatedBackups()).create(
            0, 0, 210, 20,
            Component.translatable("rollback.screen.automatedOption"),
            (button, enabled) -> expanded.setAutomatedBackups(enabled)
        ));
    }
}
