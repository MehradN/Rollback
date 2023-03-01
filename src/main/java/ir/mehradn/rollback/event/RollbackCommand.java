package ir.mehradn.rollback.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class RollbackCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            if (environment.integrated)
                dispatcher.register(CommandManager.literal("rollback")
                    .requires(RollbackCommand::hasAccessToCommand)
                    .then(CommandManager.literal("backup")
                        .then(CommandManager.literal("now")
                            .executes(RollbackCommand::backupNow))
                        .then(CommandManager.literal("delete")
                            .then(CommandManager.literal("oldest")
                                .executes((context) -> deleteBackup(context, 0)))
                            .then(CommandManager.literal("latest")
                                .executes((context) -> deleteBackup(context, 1)))
                            .then(CommandManager.argument("number", IntegerArgumentType.integer(1, RollbackConfig.getMaxBackupsPerWorld()))
                                .executes((context) -> deleteBackup(context, 2)))))
                    .then(CommandManager.literal("list")
                        .executes(RollbackCommand::listBackups)));
        }));
    }

    public static boolean hasAccessToCommand(ServerCommandSource source) {
        return ((RollbackConfig.commandAccess() == RollbackConfig.CommandAccess.ALWAYS) || source.hasPermissionLevel(4));
    }

    private static void checkIsServerHost(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Text user1 = MinecraftClient.getInstance().player.getName();
        Text user2 = context.getSource().getPlayer().getName();
        if (!user1.equals(user2))
            throw new SimpleCommandExceptionType(Text.translatable("rollback.command.unavailable")).create();
    }

    public static int backupNow(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        checkIsServerHost(context);

        Rollback.LOGGER.info("Executing the \"backup new\" command...");
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();

        boolean f = backupManager.createRollbackBackup(server);
        if (!f)
            throw new SimpleCommandExceptionType(Text.translatable("rollback.createBackup.failed")).create();

        context.getSource().sendFeedback(Text.translatable("rollback.createBackup.success"), true);
        return 1;
    }

    public static int deleteBackup(CommandContext<ServerCommandSource> context, int position) throws CommandSyntaxException {
        checkIsServerHost(context);

        Rollback.LOGGER.info("Executing the \"backup delete\" command...");
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        LevelStorage.Session session = ((MinecraftServerExpanded)server).getSession();
        String worldName = session.getLevelSummary().getName();

        int index;
        switch (position) {
            case 0 -> index = 0;
            case -1 -> index = -1;
            default -> index = IntegerArgumentType.getInteger(context, "number") - 1;
        }

        boolean f = backupManager.deleteBackup(worldName, index);
        if (!f)
            throw new SimpleCommandExceptionType(Text.translatable("rollback.deleteBackup.failed")).create();

        context.getSource().sendFeedback(Text.translatable("rollback.deleteBackup.success"), true);
        return 1;
    }

    public static int listBackups(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        checkIsServerHost(context);

        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        String worldName = ((MinecraftServerExpanded)server).getSession().getLevelSummary().getName();
        List<RollbackBackup> backups = backupManager.getRollbacksFor(worldName);

        if (backups.isEmpty()) {
            context.getSource().sendMessage(Text.translatable("rollback.command.list.noBackups"));
            return 1;
        }

        context.getSource().sendMessage(Text.translatable("rollback.command.list.title"));
        for (int i = 1; i <= backups.size(); i++) {
            RollbackBackup backup = backups.get(backups.size() - i);
            MutableText part1, part2, part3;
            part1 = Text.literal(String.format("    #%-2d    ", i));
            part2 = Text.translatable("rollback.created", backup.getDateAsString()).append(Text.literal("    "));
            part3 = Text.translatable("rollback.day", backup.daysPlayed);
            context.getSource().sendMessage(part1.append(part2.append(part3)));
        }
        return 1;
    }
}
