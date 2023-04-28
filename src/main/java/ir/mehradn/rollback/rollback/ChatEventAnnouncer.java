package ir.mehradn.rollback.rollback;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

public class ChatEventAnnouncer implements EventAnnouncer {
    CommandSource server;
    CommandSource source = null;

    public ChatEventAnnouncer(CommandSource server) {
        this.server = server;
    }

    public void setCommandSource(CommandSource source) {
        this.source = source;
    }

    public void onError(String translatableTitle, String literalInfo) {
        Component message = Component.translatable("rollback.chatEventAnnouncer.error",
            Component.translatable(translatableTitle).withStyle(ChatFormatting.BOLD),
            literalInfo
        ).withStyle(ChatFormatting.RED);
        this.server.sendSystemMessage(message);
        if (this.source != null)
            this.source.sendSystemMessage(message);
    }

    public void onSuccessfulBackup(long size) {
        Component message = Component.translatable("rollback.chatEventAnnouncer.successfulBackup", size / 1048576);
        this.server.sendSystemMessage(message);
        if (this.source != null)
            this.source.sendSystemMessage(message);
    }
}
