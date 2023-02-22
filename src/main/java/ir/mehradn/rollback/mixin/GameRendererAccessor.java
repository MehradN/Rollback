package ir.mehradn.rollback.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("updateWorldIcon")
    void InvokeUpdateWorldIcon(Path path);
}
