package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public final class ConvertScreen extends Screen {
    private final BiConsumer<Boolean, BackupType> answerConsumer;
    private final BackupType currentType;
    private final ArrayList<BackupType> allowedTypes;
    private CycleButton<BackupType> cycleButton;

    public ConvertScreen(BackupType currentType, BiConsumer<Boolean, BackupType> answerConsumer) {
        super(Component.translatable("rollback.screen.title.convertScreen"));
        this.answerConsumer = answerConsumer;
        this.currentType = currentType;

        this.allowedTypes = new ArrayList<>();
        for (BackupType type : BackupType.values())
            if (type.convertTo && type != currentType)
                this.allowedTypes.add(type);
    }

    @Override
    public void init() {
        this.cycleButton = addRenderableWidget(CycleButton.builder(ConvertScreen::toComponent)
            .withValues(this.allowedTypes)
            .create(this.width / 2 - 100, this.height / 2 - 22, 200, 20,
                Component.translatable("rollback.screen.text.newType")));
        addRenderableWidget(Button.builder(Component.translatable("rollback.screen.button.done"), this::onDone)
            .bounds(this.width / 2 - 100, this.height / 2 + 2, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("rollback.screen.button.cancel"), this::onCancel)
            .bounds(this.width / 2 + 2, this.height / 2 + 2, 98, 20).build());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        BackupType value = this.cycleButton.getValue();
        super.resize(minecraft, width, height);
        this.cycleButton.setValue(value);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (ScreenManager.getInstance() != null)
            ScreenManager.deactivate();
    }

    private static Component toComponent(BackupType type) {
        return type.toComponent();
    }

    private void onDone(Button button) {
        this.answerConsumer.accept(true, this.cycleButton.getValue());
    }

    private void onCancel(Button button) {
        this.answerConsumer.accept(false, this.currentType);
    }
}
