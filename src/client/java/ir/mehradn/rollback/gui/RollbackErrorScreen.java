package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class RollbackErrorScreen extends Screen {
    private final Runnable onAccepted;
    private final Component info;

    public RollbackErrorScreen(Component title, Component info, Runnable onAccepted) {
        super(title);
        this.info = info;
        this.onAccepted = onAccepted;
    }

    @Override
    public void init() {
        addRenderableWidget(Button.builder(Component.translatable("rollback.screen.button.ok"), (button) -> this.onAccepted.run())
            .bounds(this.width / 2 - 100, 140, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fillGradient(poseStack, 0, 0, this.width, this.height, 0xFF402020, 0xFF501010);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 90, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, this.info, this.width / 2, 110, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
