package ir.mehradn.rollback.rollback;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

public class CommandEventAnnouncer implements EventAnnouncer {
    CommandSource server;
    CommandSource source = null;

    public CommandEventAnnouncer(CommandSource server) {
        this.server = server;
    }

    public void setCommandSource(CommandSource source) {
        this.source = source;
    }

    public void onError(String translatableTitle, String literalInfo) {
        sendMessage(Component.translatable("rollback.command.error",
            Component.translatable(translatableTitle).withStyle(ChatFormatting.BOLD),
            literalInfo
        ).withStyle(ChatFormatting.RED));
    }

    public void onSuccessfulBackup(long size) {
        sendMessage(Component.translatable("rollback.command.create.successfulBackup", size / 1048576));
    }

    public void onSuccessfulDelete() {
        sendMessage(Component.translatable("rollback.command.delete.successfulDelete"));
    }

    public void onSuccessfulConvert(BackupType from, BackupType to) {
        sendMessage(Component.translatable("rollback.command.convert.successfulConvert." + from + "." + to));
    }

    private void sendMessage(Component message) {
        this.server.sendSystemMessage(message);
        if (this.source != null)
            this.source.sendSystemMessage(message);
    }
}
