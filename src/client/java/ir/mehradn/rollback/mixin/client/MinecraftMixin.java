package ir.mehradn.rollback.mixin.client;

import com.mojang.blaze3d.platform.WindowEventHandler;
import ir.mehradn.rollback.util.mixin.MinecraftExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, MinecraftExpanded {
    private List<Runnable> toRunOnThisTick = new ArrayList<>();
    private List<Runnable> toRunOnNextTick = new ArrayList<>();

    public MinecraftMixin(String string) {
        super(string);
    }

    @Override
    public void runOnNextTick(Runnable runnable) {
        this.toRunOnNextTick.add(runnable);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void runScheduledTasks(CallbackInfo ci) {
        for (Runnable task : this.toRunOnThisTick)
            task.run();
        this.toRunOnThisTick = this.toRunOnNextTick;
        this.toRunOnNextTick = new ArrayList<>();
    }
}
