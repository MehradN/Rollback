package ir.mehradn.rollback.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import ir.mehradn.rollback.util.mixin.PublicStatics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class RollbackListWidget extends AlwaysSelectedEntryListWidget<RollbackListWidget.Entry> {
    private final RollbackScreen screen;
    private final BackupManager backupManager;
    private final LevelSummary summary;
    private final CurrentSaveEntry currentSaveEntry;
    private boolean shouldReloadEntries;

    public RollbackListWidget(RollbackScreen screen, BackupManager backupManager, LevelSummary levelSummary, MinecraftClient minecraftClient,
                              int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.backupManager = backupManager;
        this.summary = levelSummary;
        this.currentSaveEntry = new CurrentSaveEntry();
        this.shouldReloadEntries = true;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.shouldReloadEntries)
            reloadEntries();
        super.render(matrices, mouseX, mouseY, delta);
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.screen.setEntrySelected(entry != null, entry != null && entry != this.currentSaveEntry);
    }

    public Optional<Entry> getSelectedAsOptional() {
        Entry entry = getSelectedOrNull();
        if (entry != null)
            return Optional.of(entry);
        return Optional.empty();
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    private void reloadEntries() {
        clearEntries();
        addEntry(this.currentSaveEntry);

        List<RollbackBackup> backups = this.backupManager.getRollbacksFor(this.summary.getName());
        for (int i = 1; i <= backups.size(); i++)
            addEntry(new RollbackEntry(i, backups.get(backups.size() - i)));

        this.screen.narrateScreenIfNarrationEnabled(true);
        this.shouldReloadEntries = false;
    }

    @Environment(EnvType.CLIENT)
    public abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        protected static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
        protected static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
        protected final MinecraftClient client;
        protected Identifier iconLocation;
        protected NativeImageBackedTexture icon;

        public Entry() {
            this.client = RollbackListWidget.this.client;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RollbackListWidget.this.setSelected(this);
            if (mouseX - RollbackListWidget.this.getRowLeft() <= 32) {
                play();
                return true;
            }
            return false;
        }

        public void close() {
            if (this.icon != null)
                this.icon.close();
        }

        public abstract void play();

        public abstract void delete();

        protected void render(Text title, Text info,
                              MatrixStack matrices, int y, int x, int mouseX, boolean hovered) {
            this.client.textRenderer.draw(matrices, title, x + 35, y + 1, 0xFFFFFF);
            this.client.textRenderer.draw(matrices, info, x + 35, y + this.client.textRenderer.fontHeight + 3, 0x808080);

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : UNKNOWN_SERVER_LOCATION);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();

            if (this.client.options.getTouchscreen().getValue() || hovered) {
                RenderSystem.setShaderTexture(0, WORLD_SELECTION_LOCATION);
                DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                int v = ((mouseX - x) < 32 ? 32 : 0);
                DrawableHelper.drawTexture(matrices, x, y, 0.0f, v, 32, 32, 256, 256);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public final class CurrentSaveEntry extends Entry {
        private final LevelSummary summary;
        private final String lastPlayed;

        public CurrentSaveEntry() {
            super();
            this.summary = RollbackListWidget.this.summary;
            this.lastPlayed = RollbackBackup.DATE_FORMAT.format(new Date(this.summary.getLastPlayed()));
            this.iconLocation = new Identifier("rollback", "backup/current_save.png");
            this.icon = getIconTexture();
        }

        public Text getNarration() {
            return Text.translatable(
                "rollback.narrator.selectCurrentSave",
                this.lastPlayed
            );
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(
                Text.translatable("rollback.screen.currentSave"),
                Text.translatable("rollback.screen.lastPlayed", this.lastPlayed),
                matrices, y, x, mouseX, hovered
            );
        }

        public void play() {
            PublicStatics.playWorld = RollbackListWidget.this.summary;
            PublicStatics.rollbackWorld = null;
            PublicStatics.recreateWorld = null;
            RollbackListWidget.this.screen.closeAndReload();
        }

        public void delete() {}

        private NativeImageBackedTexture getIconTexture() {
            Path path = this.summary.getIconPath();
            if (!Files.isRegularFile(path)) {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }

            Rollback.LOGGER.debug("Loading the world icon...");
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(inputStream);
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                this.client.getTextureManager().registerTexture(this.iconLocation, texture);
                return texture;
            } catch (IOException e) {
                Rollback.LOGGER.error("Failed to load the world icon!", e);
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public final class RollbackEntry extends Entry {
        private final int backupNumber;
        private final RollbackBackup backup;

        public RollbackEntry(int backupNumber, RollbackBackup rollbackBackup) {
            super();
            this.backupNumber = backupNumber;
            this.backup = rollbackBackup;
            this.iconLocation = new Identifier("rollback", "backup/" + this.backupNumber + "/icon.png");
            this.icon = getIconTexture();
        }

        public Text getNarration() {
            return Text.translatable(
                "rollback.narrator.selectRollback",
                this.backupNumber,
                this.backup.getDateAsString(),
                this.backup.daysPlayed
            );
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(
                Text.translatable("rollback.day", this.backup.daysPlayed),
                Text.translatable("rollback.created", this.backup.getDateAsString()),
                matrices, y, x, mouseX, hovered
            );
        }

        public void play() {
            this.client.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    if (confirmed) {
                        Rollback.LOGGER.info("Rolling back to backup #{}...", this.backupNumber);
                        boolean f = RollbackListWidget.this.backupManager.rollbackTo(this.backup);
                        if (f) {
                            PublicStatics.playWorld = RollbackListWidget.this.summary;
                            PublicStatics.rollbackWorld = null;
                            PublicStatics.recreateWorld = null;
                        }
                        RollbackListWidget.this.screen.closeAndReload();
                    } else
                        this.client.setScreen(RollbackListWidget.this.screen);
                },
                Text.translatable("rollback.screen.rollbackQuestion"),
                Text.translatable("rollback.screen.rollbackWarning"),
                Text.translatable("rollback.button"),
                Text.translatable("rollback.screen.cancel")
            ));
        }

        public void delete() {
            this.client.setScreen(new ConfirmScreen(
                (confirmed) -> {
                    if (confirmed) {
                        Rollback.LOGGER.info("Deleting the backup #{}...", this.backupNumber);
                        RollbackListWidget.this.backupManager.deleteBackup(this.backup.worldName, -this.backupNumber);
                        RollbackListWidget.this.shouldReloadEntries = true;
                    }
                    this.client.setScreen(RollbackListWidget.this.screen);
                },
                Text.translatable("rollback.screen.deleteQuestion"),
                Text.empty(),
                Text.translatable("rollback.screen.delete"),
                Text.translatable("rollback.screen.cancel")
            ));
        }

        private NativeImageBackedTexture getIconTexture() {
            if (this.backup.iconPath == null) {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }

            Path path = RollbackListWidget.this.backupManager.rollbackDirectory.resolve(this.backup.iconPath);
            if (!Files.isRegularFile(path)) {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }

            Rollback.LOGGER.debug("Loading the icon for backup #{}...", this.backupNumber);
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(inputStream);
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                this.client.getTextureManager().registerTexture(this.iconLocation, texture);
                return texture;
            } catch (IOException e) {
                Rollback.LOGGER.error("Failed to load the icon for backup #{}!", this.backupNumber, e);
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }
        }
    }
}
