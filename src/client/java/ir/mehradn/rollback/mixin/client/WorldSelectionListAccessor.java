package ir.mehradn.rollback.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(WorldSelectionList.class)
public interface WorldSelectionListAccessor {
    @Invoker("reloadWorldList")
    void invokeReloadWorldList();
}

