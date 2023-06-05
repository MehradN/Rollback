package ir.mehradn.rollback.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.ServerPacketManager;
import ir.mehradn.rollback.network.packets.OpenGUI;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.ServerBackupManager;
import ir.mehradn.rollback.util.TickTimer;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RollbackCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            dispatcher.register(Commands.literal("rollback")
                .requires(RollbackCommand::hasRequirements)
                .then(Commands.literal("create")
                    .executes((ctx) -> createBackup(ctx, null, BackupType.COMMAND))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes((ctx) -> createBackup(ctx, StringArgumentType.getString(ctx, "name"), BackupType.COMMAND))))
                .then(Commands.literal("create-manual")
                    .executes((ctx) -> createBackup(ctx, null, BackupType.MANUAL)))
                .then(Commands.literal("gui")
                    .executes(RollbackCommand::requestOpenGui)));
        });
    }

    private static boolean hasRequirements(CommandSourceStack source) {
        return (source.getPlayer() != null && source.hasPermission(4));
    }

    private static int createBackup(CommandContext<CommandSourceStack> context, String name, BackupType type) throws CommandSyntaxException {
        if (name != null && (!type.list || name.isBlank()))
            name = null;
        if (name != null && name.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.literal("The chosen name is too long! (at most 32 characters are allowed)")).create();

        ServerBackupManager backupManager = getBackupManager(context);
        try {
            backupManager.setCommandSender(context.getSource().getPlayer());
            backupManager.createBackup(type, name);
            context.getSource().sendSuccess(Component.literal("Created the backup successfully!"), true);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int requestOpenGui(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (OpenGUI.awaitingPlayers.contains(player))
            return 0;

        OpenGUI.awaitingPlayers.add(player);
        ServerTickTimer.addTimer(new TickTimer(100, () -> {
            if (OpenGUI.awaitingPlayers.contains(player)) {
                source.sendFailure(Component.literal("Failed to open the gui screen. Are you sure you have the mod installed on your client?"));
                OpenGUI.awaitingPlayers.remove(player);
            }
        }));
        ServerPacketManager.send(player, Packets.openGui, null);
        return 1;
    }

    private static ServerBackupManager getBackupManager(CommandContext<CommandSourceStack> context) {
        return ((MinecraftServerExpanded)context.getSource().getServer()).getBackupManager();
    }
}
