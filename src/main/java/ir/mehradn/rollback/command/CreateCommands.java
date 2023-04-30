package ir.mehradn.rollback.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CreateCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("create")
            .executes((ctx) -> createBackup(ctx.getSource(), null, BackupType.COMMAND))
            .then(Commands.argument("name", StringArgumentType.string())
                .executes((ctx) -> createBackup(ctx.getSource(), StringArgumentType.getString(ctx, "name"), BackupType.COMMAND)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createManualBackupCommand() {
        return Commands.literal("create-manual-backup")
            .executes((ctx) -> createBackup(ctx.getSource(), null, BackupType.MANUAL));
    }

    private static int createBackup(CommandSourceStack source, String name, BackupType type) throws CommandSyntaxException {
        assert type.manualCreation;
        if (name != null && (!type.list || name.isBlank()))
            name = null;
        if (name != null && name.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.create.nameTooLong")).create();

        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);

        try {
            backupManager.createBackup(name, type);
            source.sendSuccess(Component.translatable("rollback.command.create.success." + type), true);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }
}
