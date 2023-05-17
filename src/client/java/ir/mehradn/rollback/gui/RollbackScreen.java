package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RollbackScreen extends Screen {
    private final TabManager tabManager;
    private TabNavigationBar navigationBar;

    public RollbackScreen() {
        super(Component.translatable("rollback.screen.title"));
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    }

    @Override
    public void init() {
        this.navigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(
            new BackupListTab(BackupType.AUTOMATED, "rollback.screen.tab.automated"),
            new BackupListTab(BackupType.COMMAND, "rollback.screen.tab.command"),
            new ActionTab()).build();
        addRenderableWidget(this.navigationBar);
        this.navigationBar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.navigationBar == null)
            return;
        this.navigationBar.setWidth(this.width);
        this.navigationBar.arrangeElements();
        int y = this.navigationBar.getRectangle().bottom();
        this.tabManager.setTabArea(new ScreenRectangle(0, y + 8, this.width, this.height - y - 8));
    }

    @Override
    public void tick() {
        super.tick();
        this.tabManager.tickCurrent();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.navigationBar.keyPressed(keyCode))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
