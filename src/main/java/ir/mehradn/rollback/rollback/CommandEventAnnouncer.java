package ir.mehradn.rollback.rollback;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CommandEventAnnouncer implements EventAnnouncer {
    private final CommandSource server;
    @Nullable private CommandSource source = null;

    public CommandEventAnnouncer(CommandSource server) {
        this.server = server;
    }

    public void setCommandSource(@Nullable CommandSource source) {
        this.source = source;
    }

    @Override
    public void onError(String translatableTitle, String literalInfo) {
        sendMessage(Component.translatable("rollback.command.error",
            Component.translatable(translatableTitle).withStyle(ChatFormatting.BOLD),
            literalInfo
        ).withStyle(ChatFormatting.RED));
    }

    @Override
    public void onSuccessfulBackup(long size) {
        sendMessage(Component.translatable("rollback.command.create.successfulBackup", size / 1048576));
    }

    @Override
    public void onSuccessfulDelete() {
        sendMessage(Component.translatable("rollback.command.delete.successfulDelete"));
    }

    @Override
    public void onSuccessfulConvert(BackupType from, BackupType to) {
        sendMessage(Component.translatable("rollback.command.convert.successfulConvert." + from + "." + to));
    }

    @Override
    public void onSuccessfulConfig(boolean isDefault) {
        sendMessage(Component.translatable("rollback.command.config.successfulChange." + (isDefault ? "default" : "world")));
    }

    private void sendMessage(Component message) {
        this.server.sendSystemMessage(message);
        if (this.source != null)
            this.source.sendSystemMessage(message);
    }
}
