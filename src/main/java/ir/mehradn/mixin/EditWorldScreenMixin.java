package ir.mehradn.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    protected EditWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ButtonWidget backupButton = (ButtonWidget)this.children().stream().filter(
            btn -> (btn instanceof ButtonWidget && ((ButtonWidget) btn).getMessage().equals(Text.translatable("selectWorld.edit.backup")))
        ).findAny().get();
        ButtonWidget openBackupFolderButton = (ButtonWidget)this.children().stream().filter(
            btn -> (btn instanceof ButtonWidget && ((ButtonWidget) btn).getMessage().equals(Text.translatable("selectWorld.edit.backupFolder")))
        ).findAny().get();

        List<ButtonWidget> lowers = this.children().stream().filter(
            elm -> (elm instanceof ButtonWidget && ((ButtonWidget) elm).getY() > openBackupFolderButton.getY())
        ).map(
            elm -> (ButtonWidget)elm
        ).toList();

        int move = lowers.get(0).getY() - openBackupFolderButton.getY();
        for (ButtonWidget btn : lowers)
            btn.setY(btn.getY() - move);

        backupButton.visible = false;
        backupButton.active = false;
        openBackupFolderButton.visible = false;
        openBackupFolderButton.active = false;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("rollback.editWorld"), (button) -> {
            System.out.println("BUTTON PRESSED!!!");
        }).dimensions(backupButton.getX(), backupButton.getY(), backupButton.getWidth(), backupButton.getHeight()).build());
    }
}
