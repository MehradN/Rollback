package ir.mehradn.rollback.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RollbackCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            dispatcher.register(Commands.literal("rollback")
                .requires(RollbackCommand::hasRequirements)
                .then(Commands.literal("create")
                    .executes((ctx) -> createBackup(getBackupManager(ctx), null, BackupType.COMMAND))
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes((ctx) -> createBackup(getBackupManager(ctx), StringArgumentType.getString(ctx, "name"), BackupType.COMMAND))))
                .then(Commands.literal("create-manual")
                    .executes((ctx) -> createBackup(getBackupManager(ctx), null, BackupType.MANUAL))));
        });
    }

    private static boolean hasRequirements(CommandSourceStack source) {
        return (source.getPlayer() != null && source.hasPermission(4));
    }

    private static int createBackup(CommonBackupManager backupManager, String name, BackupType type) throws CommandSyntaxException {
        if (name != null && (!type.list || name.isBlank()))
            name = null;
        if (name != null && name.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.literal("The chosen name is too long! (at most 32 characters are allowed)")).create();
        try {
            backupManager.createBackup(name, type);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static CommonBackupManager getBackupManager(CommandContext<CommandSourceStack> context) {
        return ((MinecraftServerExpanded)context.getSource().getServer()).getBackupManager();
    }
}
