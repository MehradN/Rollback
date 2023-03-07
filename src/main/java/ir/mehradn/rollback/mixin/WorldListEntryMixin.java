package ir.mehradn.rollback.mixin;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.mixin.WorldListEntryExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin extends WorldSelectionList.Entry implements AutoCloseable, WorldListEntryExpanded {
    @Shadow @Final WorldSelectionList field_19135;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LevelSummary summary;
    @Shadow @Final private SelectWorldScreen screen;

    @Shadow protected abstract void queueLoadScreen();

    @Shadow protected abstract void loadWorld();

    public void rollbackWorld() {
        Rollback.LOGGER.debug("Opening rollback screen...");
        queueLoadScreen();
        this.minecraft.setScreen(new RollbackScreen(this.summary, (reload) -> {
            if (reload)
                ((WorldSelectionListAccessor)this.field_19135).InvokeReloadWorldList();
            this.minecraft.setScreen(this.screen);
        }));
    }

    @Inject(method = "deleteWorld", at = @At("RETURN"))
    private void deleteBackups(CallbackInfo ci) {
        BackupManager backupManager = new BackupManager();
        backupManager.deleteAllBackupsFor(this.summary.getLevelId());
    }

    @Inject(method = "loadWorld", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList$WorldListEntry;queueLoadScreen()V"))
    private void promptFeature(CallbackInfo ci) {
        if (RollbackConfig.promptDisabled())
            return;

        this.queueLoadScreen();
        BackupManager backupManager = new BackupManager();
        String worldName = this.summary.getLevelId();

        if (!backupManager.getPrompted(worldName)) {
            this.minecraft.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    backupManager.setPromptAnswer(worldName, confirmed);
                    this.loadWorld();
                },
                Component.translatable("rollback.screen.enableAutomatedQuestion"),
                Component.empty(),
                Component.translatable("gui.yes"),
                Component.translatable("gui.no")
            ));
            ci.cancel();
        }
    }
}
