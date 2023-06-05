package ir.mehradn.rollback.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ActionTab extends GridLayoutTab {
    private static final Component TITLE = Component.translatable("rollback.screen.tab.action");

    public ActionTab(RollbackScreen screen) {
        super(TITLE);
        Button openFolder;

        GridLayout.RowHelper rowHelper = this.layout.spacing(4).createRowHelper(1);
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.config"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeCommand"),
            this::onMakeCommand).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeManual"),
            this::onMakeManual).width(200).build());
        rowHelper.addChild(openFolder = Button.builder(Component.translatable("rollback.screen.button.openFolder"),
            this::onOpenFolder).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.cancel"),
            screen::onCancel).width(200).build());

        openFolder.active = screen.openFolderActivated();
    }

    private void onMakeCommand(Button button) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().createBackup();
    }

    private void onMakeManual(Button button) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().createManualBackup();
    }

    private void onOpenFolder(Button button) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().openBackupFolder();
    }
}
