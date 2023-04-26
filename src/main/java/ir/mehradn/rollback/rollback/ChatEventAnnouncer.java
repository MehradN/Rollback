package ir.mehradn.rollback.rollback;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

public class ChatEventAnnouncer {
    CommandSource source;

    public ChatEventAnnouncer(CommandSource source) {
        this.source = source;
    }

    public void onError(String translatableTitle, String literalInfo) {
        this.source.sendSystemMessage(Component.translatable("rollback.chatEventAnnouncer.error",
            Component.translatable(translatableTitle).withStyle(ChatFormatting.BOLD),
            literalInfo
        ).withStyle(ChatFormatting.RED));
    }

    public void onSuccessfulBackup(long size) {
        this.source.sendSystemMessage(Component.translatable("rollback.chatEventAnnouncer.successfulBackup", size / 1048576));
    }
}
