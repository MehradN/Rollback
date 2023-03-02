package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    private ButtonWidget gameRulesButton;

    private CyclingButtonWidget<Boolean> automatedButton;
    private int[] buttonPos;

    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "init", index = 0, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private Element changeButton(Element element) {
        ButtonWidget button = (ButtonWidget)element;

        if (RollbackConfig.replaceGameRulesButton()) {
            this.buttonPos = new int[]{button.getX(), button.getY()};
            button.setPos(this.width / 2 + 5, 151);
        } else {
            this.buttonPos = new int[]{this.width / 2 + 5, 151};
        }
        return button;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screen/world/MoreOptionsDialog;init(Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/font/TextRenderer;)V"))
    private void addMyButton(CallbackInfo ci) {
        this.automatedButton = addDrawableChild(CyclingButtonWidget.onOffBuilder(false).build(
            this.buttonPos[0], this.buttonPos[1], 150, 20,
            Text.translatable("rollback.screen.automatedOption")
        ));

        if (RollbackConfig.replaceGameRulesButton())
            this.gameRulesButton.visible = false;
        else
            this.automatedButton.visible = false;
    }

    @Inject(method = "setMoreOptionsOpen(Z)V", at = @At("RETURN"))
    private void toggleButtons(boolean moreOptionsOpen, CallbackInfo ci) {
        boolean f = RollbackConfig.replaceGameRulesButton() ^ moreOptionsOpen;
        this.gameRulesButton.visible = !f;
        this.automatedButton.visible = f;
    }

    @Inject(method = "createSession", at = @At("RETURN"))
    private void saveOption(CallbackInfoReturnable<Optional<LevelStorage.Session>> ci) {
        if (ci.getReturnValue().isEmpty())
            return;
        String worldName = ci.getReturnValue().get().getDirectoryName();
        BackupManager backupManager = new BackupManager();
        backupManager.setAutomated(worldName, this.automatedButton.getValue());
    }
}
