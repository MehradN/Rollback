package ir.mehradn.rollback.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(WorldListWidget.class)
public interface WorldListWidgetAccessor {
    @Invoker("load")
    void InvokeLoad();
}
