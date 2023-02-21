package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.util.mixin.WorldEntryExpanded;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry implements AutoCloseable, WorldEntryExpanded {
    @Shadow @Final WorldListWidget field_19135;

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private LevelSummary level;

    @Shadow @Final private SelectWorldScreen screen;

    public void rollback() {
        this.client.setScreen(new RollbackScreen(this.level, (reload) -> {
            if (reload)
                ((WorldListWidgetAccessor)field_19135).InvokeLoad();
            this.client.setScreen(this.screen);
        }));
    }
}
