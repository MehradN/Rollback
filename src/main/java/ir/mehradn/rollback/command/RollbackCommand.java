package ir.mehradn.rollback.command;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RollbackCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            dispatcher.register(Commands.literal("rollback")
                .requires(RollbackCommand::hasRequirements)
                .then(CreateCommands.createCommand())
                .then(CreateCommands.createManualBackupCommand()));
        });
    }

    private static boolean hasRequirements(CommandSourceStack source) {
        return (source.getPlayer() != null && source.hasPermission(4));
    }
}
