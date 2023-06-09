package ir.mehradn.rollback.mixin.client;

import ir.mehradn.rollback.util.mixin.GameRendererExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.nio.file.Path;
import java.util.ArrayDeque;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class GameRendererMixin implements AutoCloseable, GameRendererExpanded {
    private final ArrayDeque<Path> queuedScreenshots = new ArrayDeque<>();

    @Override
    public void takeScreenshotWithGui(Path path) {
        takeAutoScreenshot(path);
    }

    @Override
    public void queueScreenshotWithoutGui(Path path) {
        this.queuedScreenshots.add(path);
    }

    @Shadow protected abstract void takeAutoScreenshot(Path path);

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V"))
    private void takeQueuedScreenshots(CallbackInfo ci) {
        while (!this.queuedScreenshots.isEmpty())
            takeAutoScreenshot(this.queuedScreenshots.pop());
    }
}
