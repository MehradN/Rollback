package ir.mehradn.rollback.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CreateCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("create")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes((ctx) -> createBackup(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
            .executes((ctx) -> createBackup(ctx.getSource(), ""));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createManualBackupCommand() {
        return Commands.literal("create-manual-backup")
            .executes((ctx) -> createManualBackup(ctx.getSource()));
    }

    private static int createBackup(CommandSourceStack source, String name) throws CommandSyntaxException {
        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);

        if (name.isBlank())
            name = null;
        if (name != null && name.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.create.nameTooLong")).create();

        try {
            backupManager.createSpecialBackup(name, RollbackBackupType.COMMAND);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int createManualBackup(CommandSourceStack source) {
        CommonBackupManager backupManager = RollbackCommand.getBackupManager(source);

        try {
            backupManager.createNormalBackup();
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }
}
