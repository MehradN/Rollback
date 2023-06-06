package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RollbackScreen extends Screen {
    private final TabManager tabManager;
    @Nullable BackupSelectionList selectionList;
    private TabNavigationBar navigationBar;
    private BackupListTab automatedTab;
    private BackupListTab commandTab;
    private boolean showSelectionList = false;

    public RollbackScreen() {
        super(Component.translatable("rollback.screen.title.rollbackScreen"));
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    }

    public void setEntrySelected(boolean canPlay, boolean canModify) {
        boolean bl = (this.minecraft == null || !ScreenManager.isInGame(this.minecraft) || !canModify);
        this.automatedTab.setEntrySelected(canPlay && bl, canModify);
        this.commandTab.setEntrySelected(canPlay && bl, canModify);
    }

    @Override
    public void init() {
        this.automatedTab = new BackupListTab(this, BackupType.AUTOMATED, "rollback.screen.tab.automated");
        this.commandTab = new BackupListTab(this, BackupType.COMMAND, "rollback.screen.tab.command");
        setEntrySelected(false, false);

        this.navigationBar = TabNavigationBar.builder(this.tabManager, this.width)
            .addTabs(this.automatedTab, this.commandTab, new ActionTab(this)).build();
        addRenderableWidget(this.navigationBar);
        this.navigationBar.selectTab((ScreenManager.isAutomatedBackupEnabled() ? 0 : 1), false);
        this.navigationBar.setWidth(this.width);
        this.navigationBar.arrangeElements();

        int y = this.navigationBar.getRectangle().bottom();
        this.tabManager.setTabArea(new ScreenRectangle(0, y + 8, this.width, this.height - y - 8));
        this.showSelectionList = false;
        this.selectionList = new BackupSelectionList(this.minecraft, this.width, this.height, y + 8, this.height - 68, 36);
        adjustSelectionListBackupType();
    }

    @Override
    public void tick() {
        super.tick();
        this.tabManager.tickCurrent();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(poseStack);

        adjustSelectionListBackupType();
        if (this.selectionList != null && this.showSelectionList) {
            this.selectionList.render(poseStack, mouseX, mouseY, partialTick);

            Component text = Component.translatable("rollback.screen.text.total", Utils.fileSizeToString(this.selectionList.getTotalSize()));
            int width = this.font.width(text);
            drawString(poseStack, this.font, text, this.width / 2 + 154 - width, this.height - 64, 0x808080);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.navigationBar.keyPressed(keyCode))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        if (this.selectionList != null)
            this.selectionList.children().forEach(BackupSelectionList.Entry::close);
    }

    @Override
    public void onClose() {
        if (ScreenManager.getInstance() != null)
            ScreenManager.deactivate();
        else
            super.onClose();
    }

    private void adjustSelectionListBackupType() {
        Assertion.state(this.selectionList != null);
        if (this.tabManager.getCurrentTab() instanceof BackupListTab tab) {
            if (!this.showSelectionList) {
                addWidget(this.selectionList);
                this.showSelectionList = true;
            }
            this.selectionList.setBackupType(tab.backupType);
        } else {
            if (this.showSelectionList) {
                removeWidget(this.selectionList);
                this.showSelectionList = false;
                setEntrySelected(false, false);
            }
        }
    }
}
