package ir.mehradn.rollback.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.exception.BackupIOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConvertCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> convertCommand() {
        return Commands.literal("convert")
            .then(Commands.literal("automated")
                .then(Commands.argument("index", IntegerArgumentType.integer(1, RollbackConfig.MAX_MAX_AUTOMATED_BACKUPS))
                    .then(Commands.literal("command")
                        .then(Commands.argument("name", StringArgumentType.string())
                            .executes((ctx) -> convertBackup(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"),
                                BackupType.AUTOMATED, StringArgumentType.getString(ctx, "name"), BackupType.COMMAND)))
                        .executes((ctx) -> convertBackup(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"),
                            BackupType.AUTOMATED, null, BackupType.COMMAND)))
                    .then(Commands.literal("manual")
                        .executes((ctx) -> convertBackup(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"),
                            BackupType.AUTOMATED, null, BackupType.MANUAL)))))
            .then(Commands.literal("command")
                .then(Commands.argument("index", IntegerArgumentType.integer(1, RollbackConfig.MAX_MAX_COMMAND_BACKUPS))
                    .then(Commands.literal("manual")
                        .executes((ctx) -> convertBackup(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"),
                            BackupType.COMMAND, null, BackupType.MANUAL)))));
    }

    private static int convertBackup(CommandSourceStack source, int index, BackupType from, String name, BackupType to)
        throws CommandSyntaxException {
        assert from.convertFrom && to.convertTo;
        assert !from.equals(to);
        if (name != null && (!to.list || name.isBlank()))
            name = null;
        if (name != null && name.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.create.nameTooLong")).create();

        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);
        List<Integer> ids = new ArrayList<>(backupManager.world.getBackups(from).keySet());

        if (ids.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.noBackups." + from));
            return 0;
        }

        index = -index - 1;
        if (index < 0)
            index += ids.size();
        if (index < 0 || index >= ids.size())
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.invalidIndex")).create();

        Collections.sort(ids);
        int id = ids.get(index);

        try {
            backupManager.convertBackup(id, from, name, to);
            return 1;
        } catch (BackupIOException e) {
            return 0;
        }
    }
}
