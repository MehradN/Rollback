package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class NameScreen extends Screen {
    private final BiConsumer<Boolean, @Nullable String> answerConsumer;
    private final String currentName;
    private final boolean allowSameName;
    private Button doneButton;
    private EditBox nameEdit;

    public NameScreen(Component title, @Nullable String currentName, boolean allowSameName, BiConsumer<Boolean, String> answerConsumer) {
        super(title);
        this.answerConsumer = answerConsumer;
        this.currentName = (currentName == null ? "" : currentName);
        this.allowSameName = allowSameName;
    }

    @Override
    public void init() {
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 22, 200, 20,
            Component.translatable("rollback.screen.text.name"));
        this.nameEdit.setValue(this.currentName);
        this.nameEdit.setResponder(this::updateDoneButtonStatus);
        addWidget(this.nameEdit);
        this.doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, this::onDone)
            .bounds(this.width / 2 - 101, this.height / 2 + 4, 99, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, this::onCancel)
            .bounds(this.width / 2 + 2, this.height / 2 + 4, 99, 20).build());
        this.updateDoneButtonStatus(this.currentName);
    }

    @Override
    public void tick() {
        super.tick();
        this.nameEdit.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String value = this.nameEdit.getValue();
        super.resize(minecraft, width, height);
        this.nameEdit.setValue(value);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        drawString(poseStack, this.font, this.nameEdit.getMessage(), this.width / 2 - 100, this.height / 2 - 35, 0xA0A0A0);
        this.nameEdit.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (ScreenManager.getInstance() != null)
            ScreenManager.deactivate();
    }

    private void updateDoneButtonStatus(String value) {
        this.doneButton.active = this.allowSameName || !value.equals(this.currentName);
    }

    private void onDone(Button button) {
        this.answerConsumer.accept(true, this.nameEdit.getValue());
    }

    private void onCancel(Button button) {
        this.answerConsumer.accept(false, "");
    }
}
