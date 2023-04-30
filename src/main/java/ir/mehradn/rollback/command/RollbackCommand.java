package ir.mehradn.rollback.command;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.CommandEventAnnouncer;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
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
                .then(CreateCommands.createManualBackupCommand())
                .then(DeleteCommand.deleteCommand())
                .then(ConvertCommand.convertCommand())
                .then(ListCommand.listCommand()));
        });
    }

    private static boolean hasRequirements(CommandSourceStack source) {
        return (source.getPlayer() != null && source.hasPermission(4));
    }

    static CommonBackupManager getBackupManager(CommandSourceStack source) {
        CommonBackupManager backupManager = ((MinecraftServerExpanded)source.getServer()).getBackupManager();
        if (backupManager.eventAnnouncer instanceof CommandEventAnnouncer commandEventAnnouncer)
            commandEventAnnouncer.setCommandSource(source.getEntity());
        return backupManager;
    }
}
