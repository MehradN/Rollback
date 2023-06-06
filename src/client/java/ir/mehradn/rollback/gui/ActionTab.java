package ir.mehradn.rollback.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ActionTab extends GridLayoutTab {
    public ActionTab(RollbackScreen screen) {
        super(Component.translatable("rollback.screen.tab.action"));
        Button openFolder;

        GridLayout.RowHelper rowHelper = this.layout.spacing(4).createRowHelper(1);
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.config"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeCommand"),
            onClick(ScreenManager::createBackup)).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeManual"),
            onClick(ScreenManager::createManualBackup)).width(200).build());
        rowHelper.addChild(openFolder = Button.builder(Component.translatable("selectWorld.edit.backupFolder"),
            onClick(ScreenManager::openBackupFolder)).width(200).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL,
            (btn) -> screen.onClose()).width(200).build());

        openFolder.active = ScreenManager.isIntegrated(Minecraft.getInstance());
    }

    private static Button.OnPress onClick(Consumer<ScreenManager> action) {
        return (button) -> {
            ScreenManager screenManager = ScreenManager.getInstance();
            if (screenManager != null)
                action.accept(screenManager);
        };
    }
}
