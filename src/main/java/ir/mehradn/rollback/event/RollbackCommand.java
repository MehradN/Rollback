package ir.mehradn.rollback.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import ir.mehradn.rollback.util.backup.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;

@Environment(EnvType.CLIENT)
public final class RollbackCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            if (environment.includeIntegrated)
                dispatcher.register(Commands.literal("rollback")
                    .requires(RollbackCommand::hasAccessToCommand)
                    .then(Commands.literal("create")
                        .executes(RollbackCommand::createBackup))
                    .then(Commands.literal("delete")
                        .then(Commands.literal("oldest")
                            .executes((context) -> deleteBackup(context, 0)))
                        .then(Commands.literal("latest")
                            .executes((context) -> deleteBackup(context, 1)))
                        .then(Commands.argument("number", IntegerArgumentType.integer(1, RollbackConfig.maxBackupsPerWorld()))
                            .executes((context) -> deleteBackup(context, 2))))
                    .then(Commands.literal("list")
                        .executes(RollbackCommand::listBackups)));
        }));
    }

    public static boolean hasAccessToCommand(CommandSourceStack source) {
        return ((RollbackConfig.commandAccess() == RollbackConfig.CommandAccess.ALWAYS) || source.hasPermission(4));
    }

    public static int createBackup(CommandContext<CommandSourceStack> context) {
        if (isNotServerHost(context))
            return 0;

        Rollback.LOGGER.info("Executing the \"backup new\" command...");
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();

        boolean f = backupManager.createRollbackBackup(server);
        if (!f) {
            context.getSource().sendFailure(Component.translatable("rollback.createBackup.failed"));
            return 0;
        }

        context.getSource().sendSuccess(Component.translatable("rollback.createBackup.success"), true);
        return 1;
    }

    public static int deleteBackup(CommandContext<CommandSourceStack> context, int position) {
        if (isNotServerHost(context))
            return 0;

        Rollback.LOGGER.info("Executing the \"backup delete\" command...");
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        LevelStorageSource.LevelStorageAccess levelAccess = ((MinecraftServerExpanded)server).getLevelAccess();
        String worldName = levelAccess.getLevelId();

        int index;
        switch (position) {
            case 0 -> index = 0;
            case 1 -> index = -1;
            default -> index = -IntegerArgumentType.getInteger(context, "number");
        }

        boolean f = backupManager.deleteBackup(worldName, index);
        if (!f) {
            context.getSource().sendFailure(Component.translatable("rollback.deleteBackup.failed"));
            return 0;
        }

        context.getSource().sendSuccess(Component.translatable("rollback.deleteBackup.success"), true);
        return 1;
    }

    public static int listBackups(CommandContext<CommandSourceStack> context) {
        if (isNotServerHost(context))
            return 0;

        MinecraftServer server = context.getSource().getServer();
        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        RollbackWorld world = backupManager.getWorld(worldName);

        if (world.backups.isEmpty()) {
            context.getSource().sendSystemMessage(Component.translatable("rollback.command.list.noBackups"));
            return 1;
        }

        context.getSource().sendSystemMessage(Component.translatable("rollback.command.list.title"));
        for (int i = 1; i <= world.backups.size(); i++) {
            RollbackBackup backup = world.backups.get(world.backups.size() - i);
            MutableComponent part1, part2, part3;
            part1 = Component.literal(String.format("    #%-2d    ", i));
            part2 = Component.translatable("rollback.created", backup.getDateAsString()).append(Component.literal("    "));
            part3 = Component.translatable("rollback.day", backup.getDaysPlayedAsString());
            context.getSource().sendSystemMessage(part1.append(part2.append(part3)));
        }
        return 1;
    }

    private static boolean isNotServerHost(CommandContext<CommandSourceStack> context) {
        LocalPlayer player1 = Minecraft.getInstance().player;
        ServerPlayer player2 = context.getSource().getPlayer();
        if (player1 == null || player2 == null) {
            context.getSource().sendFailure(Component.translatable("rollback.command.playerOnly"));
            return true;
        }

        Component name1 = player1.getName();
        Component name2 = player2.getName();
        if (!name1.equals(name2)) {
            context.getSource().sendFailure(Component.translatable("rollback.command.unavailable"));
            return true;
        }
        return false;
    }
}
