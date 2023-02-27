package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.WorldEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry implements AutoCloseable, WorldEntryExpanded {
    @Shadow @Final
    WorldListWidget field_19135; // WorldListWidget.this
    @Shadow @Final
    private MinecraftClient client;
    @Shadow @Final
    private LevelSummary level;
    @Shadow @Final
    private SelectWorldScreen screen;

    @Shadow
    protected abstract void openReadingWorldScreen();

    public void rollback() {
        Rollback.LOGGER.debug("Opening rollback screen...");
        openReadingWorldScreen();
        this.client.setScreen(new RollbackScreen(this.level, (reload) -> {
            if (reload)
                ((WorldListWidgetAccessor)this.field_19135).InvokeLoad();
            this.client.setScreen(this.screen);
        }));
    }

    @Inject(method = "delete", at = @At("RETURN"))
    private void deleteBackups(CallbackInfo ci) {
        BackupManager backupManager = new BackupManager();
        backupManager.deleteAllBackupsFor(this.level.getName());
    }
}
