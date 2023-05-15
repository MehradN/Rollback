package ir.mehradn.rollback.gui;

import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BackupListTab implements Tab {
    private final RollbackScreen screen;
    private final Component title;
    private final BackupType backupType;
    private final BackupManager backupManager;
    private final GuiGofer gofer;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final Button rollbackButton;
    private final Button convertButton;
    private final Button deleteButton;
    private final Button renameButton;
    private final Button cancelButton;

    public BackupListTab(BackupType backupType, String title, RollbackScreen screen, BackupManager backupManager, GuiGofer gofer) {
        Assertion.argument(backupType.list, "Invalid type!");
        this.screen = screen;
        this.backupType = backupType;
        this.title = Component.translatable(title);
        this.backupManager = backupManager;
        this.gofer = gofer;

        this.rollbackButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.rollback"),
            (button) -> {}).size(150, 20).build());
        this.convertButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.convert"),
            (button) -> {}).size(150, 20).build());
        this.deleteButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.delete"),
            (button) -> {}).size(100, 20).build());
        this.renameButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.rename"),
            (button) -> {}).size(100, 20).build());
        this.cancelButton = addWidget(Button.builder(Component.translatable("rollback.screen.button.cancel"),
            (button) -> this.screen.onClose()).size(100, 20).build());
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
        this.convertButton.setPosition(mid + 4, bottom - 52);
        this.deleteButton.setPosition(mid - 154, bottom - 28);
        this.renameButton.setPosition(mid - 50, bottom - 28);
        this.cancelButton.setPosition(mid + 54, bottom - 28);
    }

    private <T extends AbstractWidget> T addWidget(T widget) {
        this.widgets.add(widget);
        return widget;
    }
}
