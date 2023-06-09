package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DirtErrorScreen extends Screen {
    private final Component message;
    private final Runnable onClose;

    public DirtErrorScreen(Component title, Component message, Runnable onClose) {
        super(title);
        this.message = message;
        this.onClose = onClose;
    }

    @Override
    public void init() {
        MultiLineTextWidget text = addRenderableWidget(new MultiLineTextWidget(0, 105, this.message, this.font)
            .setCentered(true));
        text.setX((this.width - text.getWidth()) / 2);
        addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, button -> onClose())
            .bounds(this.width / 2 - 100, 140, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 90, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.onClose.run();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
