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
    private final RollbackScreen screen;

    public ActionTab(RollbackScreen screen) {
        super(TITLE);
        this.screen = screen;

        GridLayout.RowHelper rowHelper = this.layout.spacing(4).createRowHelper(1);
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.config"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeCommand"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.makeManual"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.openFolder"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.optimizeBackups"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.optimizeFiles"),
            (button) -> { }).width(200).build());
        rowHelper.addChild(Button.builder(Component.translatable("rollback.screen.button.cancel"),
            (button) -> this.screen.onClose()).width(200).build());
    }
}
