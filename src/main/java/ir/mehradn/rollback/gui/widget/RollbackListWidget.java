package ir.mehradn.rollback.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class RollbackListWidget extends AlwaysSelectedEntryListWidget<RollbackListWidget.RollbackEntry> {
    private final RollbackScreen screen;
    private final BackupManager backupManager;
    private final String worldName;
    private boolean addedEntries = false;

    public RollbackListWidget(RollbackScreen screen, BackupManager backupManager, String worldName, MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.backupManager = backupManager;
        this.worldName = worldName;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!addedEntries)
            addEntries();
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void addEntries() {
        List<RollbackBackup> backups = backupManager.getRollbacksFor(worldName);
        for (int i = 1; i <= backups.size(); i++)
            this.addEntry(new RollbackEntry(i, backups.get(backups.size()-i)));
        this.screen.narrateScreenIfNarrationEnabled(true);
        addedEntries = true;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void setSelected(RollbackEntry entry) {
        super.setSelected(entry);
        this.screen.rollbackSelected(entry != null);
    }

    public Optional<RollbackEntry> getSelectedAsOptional() {
        RollbackEntry entry = this.getSelectedOrNull();
        if (entry != null)
            return Optional.of(entry);
        return Optional.empty();
    }

    @Environment(EnvType.CLIENT)
    public final class RollbackEntry extends AlwaysSelectedEntryListWidget.Entry<RollbackEntry> implements AutoCloseable {
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
        private static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
        private static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
        private final MinecraftClient client;
        private final int backupNumber;
        private final RollbackBackup backup;
        private final Date creationDate;
        private final Identifier iconLocation;
        private final NativeImageBackedTexture icon;

        public RollbackEntry(int backupNumber, RollbackBackup rollbackBackup) {
            this.client = RollbackListWidget.this.client;
            this.backupNumber = backupNumber;
            this.backup = rollbackBackup;
            this.creationDate = Date.from(backup.backupTime.toInstant(ZoneOffset.UTC));
            this.iconLocation = new Identifier("rollback", "backup/" + this.backupNumber + "/icon.png");
            this.icon = getIconTexture();
        }

        public Text getNarration() {
            return Text.translatable("rollback.narrator.selectRollback",
                    backupNumber,
                    creationDate,
                    backup.daysPlayed
            );
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Text title = Text.translatable("rollback.screen.day", backup.daysPlayed);
            Text created = Text.translatable("rollback.screen.created", DATE_FORMAT.format(creationDate));
            this.client.textRenderer.draw(matrices, title, (float)(x + 32 + 3), (float)(y + 1), 0xFFFFFF);
            this.client.textRenderer.draw(matrices, created, (float)(x + 32 + 3), (float)(y + this.client.textRenderer.fontHeight + 3), 0x808080);

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

                int j = ((mouseX - x) < 32 ? 32 : 0);
                DrawableHelper.drawTexture(matrices, x, y, 0.0f, j, 32, 32, 256, 256);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RollbackListWidget.this.setSelected(this);
            if (mouseX - RollbackListWidget.this.getRowLeft() <= 32) {
                this.play();
                return true;
            }
            return false;
        }

        public NativeImageBackedTexture getIconTexture() {
            if (backup.iconPath == null) {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }

            Path path = Path.of(RollbackListWidget.this.backupManager.rollbackDirectory.toString(), backup.iconPath.toString());
            if (!Files.isRegularFile(path)) {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }

            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(inputStream);
                Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");

                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                this.client.getTextureManager().registerTexture(this.iconLocation, texture);
                return texture;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void play() {
            System.out.println("ROLLBACK_SCREEN: rollbackButton");
        }

        public void close() {
            if (this.icon != null)
                icon.close();
        }
    }
}
