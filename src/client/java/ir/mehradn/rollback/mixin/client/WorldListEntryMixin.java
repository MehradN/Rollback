package ir.mehradn.rollback.mixin.client;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.rollback.ClientBackupManager;
import ir.mehradn.rollback.util.mixin.WorldListEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin extends WorldSelectionList.Entry implements AutoCloseable, WorldListEntryExpanded {
    @Shadow @Final private SelectWorldScreen screen;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LevelSummary summary;
    @Shadow @Final WorldSelectionList field_19135;

    @Shadow public abstract void joinWorld();

    @Shadow public abstract void recreateWorld();

    @Override
    public void rollbackWorld() {
        Rollback.LOGGER.debug("Opening rollback screen...");
        queueLoadScreen();
        ScreenManager.activate(this.minecraft, new ClientBackupManager(this.minecraft, this.summary), (action, lastScreen) -> {
            switch (action) {
                case NOTHING -> this.minecraft.setScreen(this.screen);
                case RELOAD -> {
                    ((WorldSelectionListAccessor)this.field_19135).invokeReloadWorldList();
                    this.minecraft.setScreen(this.screen);
                }
                case PLAY -> joinWorld();
                case RECREATE -> recreateWorld();
                case ROLLBACK -> rollbackWorld();
            }
        });
    }

    @Shadow protected abstract void queueLoadScreen();
}
