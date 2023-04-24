package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import ir.mehradn.rollback.util.backup.RollbackWorld;
import ir.mehradn.rollback.util.mixin.WorldSelectionListCallbackAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class RollbackSelectionList extends ObjectSelectionList<RollbackSelectionList.Entry> {
    private final RollbackScreen screen;
    private final BackupManager backupManager;
    private final RollbackWorld rollbackWorld;
    private final LevelSummary summary;
    private final CurrentSaveEntry currentSaveEntry;
    private boolean shouldReloadEntries;

    public RollbackSelectionList(RollbackScreen screen, BackupManager backupManager, RollbackWorld rollbackWorld,
                                 LevelSummary levelSummary, Minecraft minecraftClient,
                                 int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.backupManager = backupManager;
        this.rollbackWorld = rollbackWorld;
        this.summary = levelSummary;
        this.currentSaveEntry = new CurrentSaveEntry();
        this.shouldReloadEntries = true;
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        if (this.shouldReloadEntries)
            reloadEntries();
        super.render(poseStack, mouseX, mouseY, delta);
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.screen.setEntrySelected(entry != null, entry != null && entry != this.currentSaveEntry);
    }

    public Optional<Entry> getSelectedOpt() {
        Entry entry = getSelected();
        if (entry != null)
            return Optional.of(entry);
        return Optional.empty();
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    private void reloadEntries() {
        clearEntries();
        addEntry(this.currentSaveEntry);

        for (int i = 1; i <= this.rollbackWorld.backups.size(); i++)
            addEntry(new RollbackEntry(i, this.rollbackWorld.backups.get(this.rollbackWorld.backups.size() - i)));

        this.screen.triggerImmediateNarration(true);
        this.shouldReloadEntries = false;
    }

    @Environment(EnvType.CLIENT)
    public abstract class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
        protected static final ResourceLocation UNKNOWN_SERVER_LOCATION = new ResourceLocation("textures/misc/unknown_server.png");
        protected static final ResourceLocation WORLD_SELECTION_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
        protected final Minecraft minecraft;
        protected ResourceLocation iconLocation;
        protected DynamicTexture icon;

        public Entry() {
            this.minecraft = RollbackSelectionList.this.minecraft;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RollbackSelectionList.this.setSelected(this);
            if (mouseX - RollbackSelectionList.this.getRowLeft() <= 32) {
                playBackup();
                return true;
            }
            return false;
        }

        public void close() {
            if (this.icon != null)
                this.icon.close();
        }

        public abstract void playBackup();

        public abstract void deleteBackup();

        protected void render(Component title, Component info1, Component info2,
                              PoseStack poseStack, int y, int x, int mouseX, boolean hovered) {
            this.minecraft.font.draw(poseStack, title, x + 35, y + 1, 0xFFFFFF);
            this.minecraft.font.draw(poseStack, info1, x + 35, y + this.minecraft.font.lineHeight + 3, 0x808080);
            this.minecraft.font.draw(poseStack, info2, x + 35, y + 2 * this.minecraft.font.lineHeight + 3, 0x808080);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : UNKNOWN_SERVER_LOCATION);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();

            if (this.minecraft.options.touchscreen().get() || hovered) {
                RenderSystem.setShaderTexture(0, WORLD_SELECTION_LOCATION);
                GuiComponent.fill(poseStack, x, y, x + 32, y + 32, 0xA0909090);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                int v = ((mouseX - x) < 32 ? 32 : 0);
                GuiComponent.blit(poseStack, x, y, 0.0f, v, 32, 32, 256, 256);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public final class CurrentSaveEntry extends Entry {
        private final LevelSummary summary;
        private final String lastPlayed;

        public CurrentSaveEntry() {
            super();
            this.summary = RollbackSelectionList.this.summary;
            this.lastPlayed = RollbackBackup.DATE_FORMAT.format(new Date(this.summary.getLastPlayed()));
            this.iconLocation = new ResourceLocation("rollback", "backup/current_save.png");
            this.icon = getIconTexture();
        }

        public @NotNull Component getNarration() {
            return Component.translatable("rollback.narrator.selectCurrentSave", this.lastPlayed);
        }

        public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(
                Component.translatable("rollback.screen.currentSave"),
                Component.translatable("rollback.screen.lastPlayed", this.lastPlayed),
                Component.empty(),
                poseStack, y, x, mouseX, hovered
            );
        }

        public void playBackup() {
            RollbackSelectionList.this.screen.doAction(WorldSelectionListCallbackAction.JOIN_WORLD);
        }

        public void deleteBackup() { }

        private DynamicTexture getIconTexture() {
            Path path = this.summary.getIcon();
            if (!Files.isRegularFile(path)) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }

            Rollback.LOGGER.debug("Loading the world icon...");
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(inputStream);
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                DynamicTexture texture = new DynamicTexture(image);
                this.minecraft.getTextureManager().register(this.iconLocation, texture);
                return texture;
            } catch (IOException e) {
                Rollback.LOGGER.error("Failed to load the world icon!", e);
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public final class RollbackEntry extends Entry {
        private final int backupNumber;
        private final String worldName;
        private final RollbackBackup backup;

        public RollbackEntry(int backupNumber, RollbackBackup rollbackBackup) {
            super();
            this.backupNumber = backupNumber;
            this.worldName = RollbackSelectionList.this.summary.getLevelId();
            this.backup = rollbackBackup;
            this.iconLocation = new ResourceLocation("rollback", "backup/" + this.backupNumber + "/icon.png");
            this.icon = getIconTexture();
        }

        public @NotNull Component getNarration() {
            if (this.backup.name == null) {
                return Component.translatable(
                    "rollback.narrator.selectRollback",
                    this.backupNumber,
                    this.backup.getDateAsString(),
                    this.backup.getDaysPlayedAsString()
                );
            } else {
                return Component.translatable(
                    "rollback.narrator.selectNamedRollback",
                    this.backupNumber,
                    this.backup.name,
                    this.backup.getDateAsString(),
                    this.backup.getDaysPlayedAsString()
                );
            }
        }

        public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.backup.name == null) {
                super.render(
                    Component.translatable("rollback.day", this.backup.getDaysPlayedAsString()),
                    Component.translatable("rollback.created", this.backup.getDateAsString()),
                    Component.empty(),
                    poseStack, y, x, mouseX, hovered
                );
            } else {
                super.render(
                    Component.literal(this.backup.name),
                    Component.translatable("rollback.created", this.backup.getDateAsString()),
                    Component.translatable("rollback.day", this.backup.getDaysPlayedAsString()),
                    poseStack, y, x, mouseX, hovered
                );
            }
        }

        public void playBackup() {
            this.minecraft.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    if (confirmed) {
                        Rollback.LOGGER.info("Rolling back to backup #{}...", this.backupNumber);
                        boolean f = RollbackSelectionList.this.backupManager.rollbackTo(this.worldName, this.backup);
                        if (f)
                            RollbackSelectionList.this.screen.doAction(WorldSelectionListCallbackAction.JOIN_WORLD);
                        else
                            RollbackSelectionList.this.screen.doAction(WorldSelectionListCallbackAction.RELOAD_WORLD_LIST);
                    } else
                        this.minecraft.setScreen(RollbackSelectionList.this.screen);
                },
                Component.translatable("rollback.screen.rollbackQuestion"),
                Component.translatable("rollback.screen.rollbackWarning"),
                Component.translatable("rollback.button"),
                Component.translatable("gui.cancel")
            ));
        }

        public void deleteBackup() {
            this.minecraft.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    if (confirmed) {
                        Rollback.LOGGER.info("Deleting the backup #{}...", this.backupNumber);
                        RollbackSelectionList.this.backupManager.deleteBackup(this.worldName, -this.backupNumber);
                        RollbackSelectionList.this.shouldReloadEntries = true;
                    }
                    this.minecraft.setScreen(RollbackSelectionList.this.screen);
                },
                Component.translatable("rollback.screen.deleteQuestion"),
                Component.empty(),
                Component.translatable("selectWorld.deleteButton"),
                Component.translatable("gui.cancel")
            ));
        }

        private DynamicTexture getIconTexture() {
            if (this.backup.iconPath == null) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }

            Path path = RollbackSelectionList.this.backupManager.rollbackDirectory.resolve(this.backup.iconPath);
            if (!Files.isRegularFile(path)) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }

            Rollback.LOGGER.debug("Loading the icon for backup #{}...", this.backupNumber);
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(inputStream);
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                DynamicTexture texture = new DynamicTexture(image);
                this.minecraft.getTextureManager().register(this.iconLocation, texture);
                return texture;
            } catch (IOException e) {
                Rollback.LOGGER.error("Failed to load the icon for backup #{}!", this.backupNumber, e);
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }
        }
    }
}
