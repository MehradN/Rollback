package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BackupSelectionList extends ObjectSelectionList<BackupSelectionList.Entry> {
    private final ScreenManager screenManager;
    private final BackupManager backupManager;
    private final CurrentSaveEntry currentSaveEntry;
    private boolean shouldReloadEntries;
    private BackupType backupType = BackupType.AUTOMATED;
    private long fileSizeSum;

    public BackupSelectionList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.screenManager = ScreenManager.getInstance();
        Assertion.state(this.screenManager != null, "Create a screen manager before this!");
        this.backupManager = this.screenManager.backupManager;
        this.currentSaveEntry = new CurrentSaveEntry();
        suggestReloadingEntries();
    }

    public void suggestReloadingEntries() {
        this.shouldReloadEntries = true;
    }

    public void setBackupType(BackupType type) {
        if (this.backupType != type) {
            this.backupType = type;
            suggestReloadingEntries();
        }
    }

    public long getTotalSize() {
        return this.fileSizeSum;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.shouldReloadEntries)
            reloadEntries();
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.screenManager.rollbackScreen.setEntrySelected(entry != null, entry != null && entry != this.currentSaveEntry);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    private void reloadEntries() {
        clearEntries();
        setSelected(null);
        addEntry(this.currentSaveEntry);
        centerScrollOn(this.currentSaveEntry);

        List<Integer> backups = new ArrayList<>(this.backupManager.getWorld().getBackups(this.backupType).keySet());
        Collections.sort(backups);
        this.fileSizeSum = 0;
        for (int i = 1; i <= backups.size(); i++) {
            int id = backups.get(backups.size() - i);
            addEntry(new BackupEntry(i, id));
            long fileSize = this.backupManager.getWorld().getBackup(id, this.backupType).fileSize;
            if (fileSize >= 0)
                this.fileSizeSum += fileSize;
        }

        this.screenManager.rollbackScreen.triggerImmediateNarration(true);
        this.shouldReloadEntries = false;
    }

    @Environment(EnvType.CLIENT)
    public abstract class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
        private static final ResourceLocation WORLD_SELECTION = new ResourceLocation("textures/gui/world_selection.png");
        private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
        protected final Minecraft minecraft;
        protected final ScreenManager screenManager;
        private final ResourceLocation iconLocation;
        @Nullable private DynamicTexture icon;

        protected Entry(String iconLocation) {
            this.minecraft = BackupSelectionList.this.minecraft;
            this.screenManager = BackupSelectionList.this.screenManager;
            this.iconLocation = new ResourceLocation(Rollback.MOD_ID, iconLocation);
        }

        public abstract void playEntry();

        public abstract void deleteEntry();

        public abstract void convertEntry();

        public abstract void renameEntry();

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            BackupSelectionList.this.setSelected(this);
            if (mouseX - BackupSelectionList.this.getRowLeft() <= 32) {
                playEntry();
                return true;
            }
            return false;
        }

        @Override
        public void close() {
            if (this.icon != null)
                this.icon.close();
        }

        protected void render(Component title, Component info1, Component info2,
                              PoseStack poseStack, int top, int left, int mouseX, boolean isMouseOver) {
            this.minecraft.font.draw(poseStack, title, left + 35, top + 1, 0xFFFFFF);
            this.minecraft.font.draw(poseStack, info1, left + 35, top + this.minecraft.font.lineHeight + 3, 0x808080);
            this.minecraft.font.draw(poseStack, info2, left + 35, top + 2 * this.minecraft.font.lineHeight + 3, 0x808080);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : UNKNOWN_SERVER);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, left, top, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();

            if (this.minecraft.options.touchscreen().get() || isMouseOver) {
                RenderSystem.setShaderTexture(0, WORLD_SELECTION);
                GuiComponent.fill(poseStack, left, top, left + 32, top + 32, 0xA0909090);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                int v = ((mouseX - left) < 32 ? 32 : 0);
                GuiComponent.blit(poseStack, left, top, 0.0f, v, 32, 32, 256, 256);
            }
        }

        protected abstract @Nullable NativeImage getIconImage();

        protected void buildIconTexture() {
            Rollback.LOGGER.debug("Loading the entry icon...");
            NativeImage image = getIconImage();
            if (image != null) {
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                this.icon = new DynamicTexture(image);
                this.minecraft.getTextureManager().register(this.iconLocation, this.icon);
                return;
            }
            this.minecraft.getTextureManager().release(this.iconLocation);
            this.icon = null;
        }

        protected void unsupportedOperation() {
            //noinspection DataFlowIssue
            Assertion.runtime(false, "This operation is not supported for this kind of entry!");
        }
    }

    @Environment(EnvType.CLIENT)
    public class CurrentSaveEntry extends Entry {
        public CurrentSaveEntry() {
            super("backup_selection_list/current_save.png");
            buildIconTexture();
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(
                Component.translatable("rollback.screen.text.currentSave"),
                this.screenManager.currentSaveLastPlayed(),
                Component.empty(),
                poseStack, top, left, mouseX, isMouseOver
            );
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("rollback.screen.narrator.currentSave", this.screenManager.currentSaveLastPlayed());
        }

        @Override
        public void playEntry() {
            this.screenManager.playCurrentSave();
        }

        @Override
        public void deleteEntry() {
            unsupportedOperation();
        }

        @Override
        public void convertEntry() {
            unsupportedOperation();
        }

        @Override
        public void renameEntry() {
            unsupportedOperation();
        }

        @Override
        protected NativeImage getIconImage() {
            return this.screenManager.loadCurrentSaveIcon();
        }
    }

    @Environment(EnvType.CLIENT)
    public class BackupEntry extends Entry {
        private final int index;
        private final int backupID;
        private final BackupType backupType;
        private final RollbackBackup backup;

        public BackupEntry(int index, int backupID) {
            super("backup_selection_list/backup/" + backupID + "/icon.png");
            this.index = index;
            this.backupID = backupID;
            this.backupType = BackupSelectionList.this.backupType;
            this.backup = BackupSelectionList.this.backupManager.getWorld().getBackup(backupID, this.backupType);
            buildIconTexture();
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            Component name;
            if (this.backup.name == null)
                name = Component.translatable("rollback.screen.text.day", this.backup.getDaysPlayedAsString());
            else
                name = Component.literal(this.backup.name);

            super.render(
                name,
                Component.translatable("rollback.screen.text.creationDate", this.backup.getDateAsString()),
                Component.translatable("rollback.screen.text.backupInfo",
                    this.backup.getDaysPlayedAsString(),
                    Utils.fileSizeToString(this.backup.fileSize),
                    this.backupID),
                poseStack, top, left, mouseX, isMouseOver
            );
        }

        @Override
        public @NotNull Component getNarration() {
            String date = this.backup.getDateAsString();
            String days = this.backup.getDaysPlayedAsString();
            String size = Utils.fileSizeToString(this.backup.fileSize);
            if (this.backup.name == null)
                return Component.translatable("rollback.screen.narrator.unnamedBackup", this.index, date, days, size);
            else
                return Component.translatable("rollback.screen.narrator.nameBackup", this.index, this.backup.name, date, days, size);
        }

        @Override
        public void playEntry() {
            this.screenManager.rollbackToBackup(this.backupID, this.backupType);
        }

        @Override
        public void deleteEntry() {
            this.screenManager.deleteBackup(this.backupID, this.backupType);
        }

        @Override
        public void convertEntry() {
            this.screenManager.convertBackup(this.backupID, this.backupType);
        }

        @Override
        public void renameEntry() {
            this.screenManager.renameBackup(this.backupID, this.backupType);
        }

        @Override
        protected @Nullable NativeImage getIconImage() {
            if (this.backup.iconPath == null)
                return null;
            Path path = this.minecraft.getLevelSource().getBackupPath().resolve("rollbacks").resolve(this.backup.iconPath);
            if (!Files.isRegularFile(path))
                return null;

            Rollback.LOGGER.debug("Loading the icon for backup #{}...", this.backupID);
            try (InputStream inputStream = Files.newInputStream(path)) {
                return NativeImage.read(inputStream);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
