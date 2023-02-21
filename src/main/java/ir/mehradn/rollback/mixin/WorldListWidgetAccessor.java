package ir.mehradn.rollback.mixin;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldListWidget.class)
public interface WorldListWidgetAccessor {
    @Invoker("load")
    void InvokeLoad();
}
