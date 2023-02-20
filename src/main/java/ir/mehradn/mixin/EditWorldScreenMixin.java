package ir.mehradn.mixin;

import ir.mehradn.util.PublicStatics;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final private BooleanConsumer callback;

    @Shadow @Final private LevelStorage.Session storageSession;

    protected EditWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButtons(CallbackInfo ci) {
        ButtonWidget backupButton = (ButtonWidget)this.children().stream().filter(
            btn -> (btn instanceof ButtonWidget && ((ButtonWidget) btn).getMessage().equals(Text.translatable("selectWorld.edit.backup")))
        ).findAny().get();
        ButtonWidget openBackupFolderButton = (ButtonWidget)this.children().stream().filter(
            btn -> (btn instanceof ButtonWidget && ((ButtonWidget) btn).getMessage().equals(Text.translatable("selectWorld.edit.backupFolder")))
        ).findAny().get();

        backupButton.visible = false;
        backupButton.active = false;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.editWorld.button"), (button) -> {
            System.out.println("BUTTON PRESSED!!!");
        }).dimensions(backupButton.getX(), backupButton.getY(), backupButton.getWidth(), backupButton.getHeight()).build());

        openBackupFolderButton.visible = false;
        openBackupFolderButton.active = false;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.editWorld.recreate"), (button) -> {
            PublicStatics.recreateWorld = this.storageSession.getLevelSummary();
            this.callback.accept(false);
        }).dimensions(openBackupFolderButton.getX(), openBackupFolderButton.getY(), openBackupFolderButton.getWidth(), openBackupFolderButton.getHeight()).build());
    }
}
