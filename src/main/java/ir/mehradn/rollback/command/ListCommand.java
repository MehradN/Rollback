package ir.mehradn.rollback.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ListCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> listCommand() {
        return Commands.literal("list")
            .then(Commands.literal("automated-backups")
                .executes((ctx) -> listBackups(ctx.getSource(), RollbackBackupType.AUTOMATED)))
            .then(Commands.literal("command-backups")
                .executes((ctx) -> listBackups(ctx.getSource(), RollbackBackupType.COMMAND)));
    }

    private static int listBackups(CommandSourceStack source, RollbackBackupType type) {
        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);
        List<RollbackBackup> backups = new ArrayList<>(new TreeMap<>(backupManager.world.getBackups(type)).values());
        String typeTranslate = type.toString().toLowerCase();

        if (backups.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.list.noBackups." + typeTranslate));
            return 0;
        }

        source.sendSystemMessage(Component.translatable("rollback.command.list.title." + typeTranslate));
        for (int i = 0; i < backups.size(); i++) {
            RollbackBackup backup = backups.get(i);
            Component text;
            String index = String.format("%02d", i);
            String date = backup.getDateAsString();
            String day = backup.getDaysPlayedAsString();
            if (backup.name == null)
                text = Component.translatable("rollback.command.list.item", index, date, day);
            else
                text = Component.translatable("rollback.command.list.itemNamed", index, date, day, backup.name);
            source.sendSystemMessage(text);
        }
        return 1;
    }
}
