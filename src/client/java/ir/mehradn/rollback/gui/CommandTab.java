package ir.mehradn.rollback.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CommandTab implements Tab {
    private static final Component title = Component.translatable("rollback.screen.tab.command");

    @Override
    public @NotNull Component getTabTitle() {
        return title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {

    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {

    }
}
