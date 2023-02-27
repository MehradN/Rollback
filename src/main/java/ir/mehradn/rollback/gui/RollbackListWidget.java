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
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class RollbackListWidget extends AlwaysSelectedEntryListWidget<RollbackListWidget.RollbackEntry> {
    private final RollbackScreen screen;
    private final BackupManager backupManager;
    private final LevelSummary summary;
    private boolean shouldReloadEntries;

    public RollbackListWidget(RollbackScreen screen, BackupManager backupManager, LevelSummary levelSummary, MinecraftClient minecraftClient,
                              int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.backupManager = backupManager;
        this.summary = levelSummary;
        this.shouldReloadEntries = true;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.shouldReloadEntries)
            reloadEntries();
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void reloadEntries() {
        clearEntries();
        List<RollbackBackup> backups = this.backupManager.getRollbacksFor(this.summary.getName());
        for (int i = 1; i <= backups.size(); i++)
            addEntry(new RollbackEntry(i, backups.get(backups.size() - i)));
        this.screen.narrateScreenIfNarrationEnabled(true);
        this.shouldReloadEntries = false;
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void setSelected(RollbackEntry entry) {
        super.setSelected(entry);
        this.screen.setEntrySelected(entry != null);
    }

    public Optional<RollbackEntry> getSelectedAsOptional() {
        RollbackEntry entry = getSelectedOrNull();
        if (entry != null)
            return Optional.of(entry);
        return Optional.empty();
    }

    @Environment(EnvType.CLIENT)
    public final class RollbackEntry extends AlwaysSelectedEntryListWidget.Entry<RollbackEntry> implements AutoCloseable {
        private static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
        private static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
        private final MinecraftClient client;
        private final int backupNumber;
        private final RollbackBackup backup;
        private final Identifier iconLocation;
        private final NativeImageBackedTexture icon;

        public RollbackEntry(int backupNumber, RollbackBackup rollbackBackup) {
            this.client = RollbackListWidget.this.client;
            this.backupNumber = backupNumber;
            this.backup = rollbackBackup;
            this.iconLocation = new Identifier("rollback", "backup/" + this.backupNumber + "/icon.png");
            this.icon = getIconTexture();
        }

        public Text getNarration() {
            return Text.translatable("rollback.narrator.selectRollback",
                this.backupNumber,
                this.backup.getDateAsString(),
                this.backup.daysPlayed
            );
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Text title = Text.translatable("rollback.day", this.backup.daysPlayed);
            Text created = Text.translatable("rollback.created", this.backup.getDateAsString());
            this.client.textRenderer.draw(matrices, title, x + 35, y + 1, 0xFFFFFF);
            this.client.textRenderer.draw(matrices, created, x + 35, y + this.client.textRenderer.fontHeight + 3, 0x808080);

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

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RollbackListWidget.this.setSelected(this);
            if (mouseX - RollbackListWidget.this.getRowLeft() <= 32) {
                play();
                return true;
            }
            return false;
        }

        public NativeImageBackedTexture getIconTexture() {
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
                        RollbackListWidget.this.backupManager.deleteBackup(this.backup.worldName, this.backupNumber);
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

        public void close() {
            if (this.icon != null)
                this.icon.close();
        }
    }
}
