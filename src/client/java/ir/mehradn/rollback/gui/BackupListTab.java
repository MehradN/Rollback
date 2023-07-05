package ir.mehradn.rollback.gui;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BackupListTab implements Tab {
    public final BackupType backupType;
    private final RollbackScreen screen;
    private final Component title;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final Button rollbackButton;
    private final Button convertButton;
    private final Button deleteButton;
    private final Button renameButton;
    private final Button cancelButton;

    public BackupListTab(RollbackScreen screen, BackupType backupType, String title) {
        this.backupType = backupType;
        this.screen = screen;
        this.title = Component.translatable(title);

        this.rollbackButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.rollback." + backupType.toString()),
            onClick(BackupSelectionList.Entry::playEntry)).size(152, 20).build());
        this.convertButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.convert"),
            onClick(BackupSelectionList.Entry::convertEntry)).size(152, 20).build());
        this.deleteButton = addWidget(Button.builder(Component.translatable("selectWorld.delete"),
            onClick(BackupSelectionList.Entry::deleteEntry)).size(100, 20).build());
        this.renameButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.rename"),
            onClick(BackupSelectionList.Entry::renameEntry)).size(100, 20).build());
        this.cancelButton = addWidget(Button.builder(CommonComponents.GUI_CANCEL,
            (btn) -> screen.onClose()).size(100, 20).build());

        setEntrySelected(false, false);
    }

    public void setEntrySelected(boolean canPlay, boolean canModify) {
        this.rollbackButton.active = canPlay;
        this.convertButton.active = canModify;
        this.deleteButton.active = canModify;
        this.renameButton.active = canModify;
    }

    @Override
    public @NotNull Component getTabTitle() {
        return this.title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        for (AbstractWidget widget : this.widgets)
            widget.visitWidgets(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle rect) {
        int mid = rect.position().x() + rect.width() / 2;
        int bottom = rect.position().y() + rect.height();
        this.rollbackButton.setPosition(mid - 154, bottom - 52);
        this.convertButton.setPosition(mid + 2, bottom - 52);
        this.deleteButton.setPosition(mid - 154, bottom - 28);
        this.renameButton.setPosition(mid - 50, bottom - 28);
        this.cancelButton.setPosition(mid + 54, bottom - 28);
    }

    private <T extends AbstractWidget> T addWidget(T widget) {
        this.widgets.add(widget);
        return widget;
    }

    private Button.OnPress onClick(Consumer<BackupSelectionList.Entry> action) {
        return (button) -> {
            if (this.screen.selectionList != null && this.screen.selectionList.getSelected() != null)
                action.accept(this.screen.selectionList.getSelected());
        };
    }
}
