package ir.mehradn.rollback.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ListCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> listCommand() {
        return Commands.literal("list")
            .executes((ctx) -> listBackups(ctx.getSource(), BackupType.AUTOMATED, RollbackConfig.MAX_MAX_AUTOMATED_BACKUPS))
            .then(Commands.literal("automated")
                .executes((ctx) -> listBackups(ctx.getSource(), BackupType.AUTOMATED, RollbackConfig.MAX_MAX_AUTOMATED_BACKUPS)))
            .then(Commands.literal("command")
                .executes((ctx) -> listBackups(ctx.getSource(), BackupType.COMMAND, RollbackConfig.MAX_MAX_AUTOMATED_BACKUPS))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, RollbackConfig.MAX_MAX_COMMAND_BACKUPS))
                    .executes((ctx) -> listBackups(ctx.getSource(), BackupType.COMMAND, IntegerArgumentType.getInteger(ctx, "count")))));
    }

    private static int listBackups(CommandSourceStack source, BackupType type, int count) {
        assert type.list;
        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);
        List<RollbackBackup> backups = new ArrayList<>(new TreeMap<>(backupManager.world.getBackups(type)).values());

        if (backups.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.noBackups." + type));
            return 0;
        }

        count = Math.min(backups.size(), count);
        source.sendSystemMessage(Component.translatable("rollback.command.list.title." + type));
        for (int i = 1; i <= count; i++) {
            RollbackBackup backup = backups.get(backups.size() - i);
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
        if (count < backups.size())
            source.sendSystemMessage(Component.translatable("rollback.command.list.more"));
        return 1;
    }
}
