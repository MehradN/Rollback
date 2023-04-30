package ir.mehradn.rollback.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.exception.BackupIOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeleteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> deleteCommand() {
        return Commands.literal("delete")
            .then(Commands.literal("oldest")
                .executes((ctx) -> deleteBackup(ctx.getSource(), BackupType.COMMAND, -1)))
            .then(Commands.literal("latest")
                .executes((ctx) -> deleteBackup(ctx.getSource(), BackupType.COMMAND, 0)))
            .then(Commands.argument("index", IntegerArgumentType.integer(1, RollbackConfig.MAX_MAX_COMMAND_BACKUPS))
                .executes((ctx) -> deleteBackup(ctx.getSource(), BackupType.COMMAND, IntegerArgumentType.getInteger(ctx, "index") - 1)));
    }

    private static int deleteBackup(CommandSourceStack source, BackupType type, int index) throws CommandSyntaxException {
        assert type.manualDeletion;
        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);
        List<Integer> ids = new ArrayList<>(backupManager.world.getBackups(type).keySet());

        if (ids.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.noBackups." + type));
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
            backupManager.deleteBackup(id, type);
            return 1;
        } catch (BackupIOException e) {
            return 0;
        }
    }
}
