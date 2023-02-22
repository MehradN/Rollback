package ir.mehradn.rollback.event;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.util.backup.BackupManager;
import ir.mehradn.rollback.util.backup.RollbackBackup;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.List;

public class CommandHandler {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            if (environment.integrated)
                dispatcher.register(CommandManager.literal("rollback")
                        .requires(CommandHandler::hasAccessToCommand)
                        .then(CommandManager.literal("backup")
                                .then(CommandManager.literal("now")
                                        .executes(CommandHandler::backupNow))
                                .then(CommandManager.literal("delete")
                                        .then(CommandManager.literal("oldest")
                                                .executes(CommandHandler::deleteOldestBackup))
                                        .then(CommandManager.literal("latest")
                                                .executes(CommandHandler::deleteLatestBackup))))
                        .then(CommandManager.literal("list")
                                .executes(CommandHandler::listBackups)));
        }));
    }

    public static boolean hasAccessToCommand(ServerCommandSource source) {
        return true;
    }

    public static int backupNow(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded) server).getBackupManager();
        if (!backupManager.createRollbackBackup(server))
            throw new SimpleCommandExceptionType(Text.translatable("rollback.command.backupNow.failed")).create();
        context.getSource().sendFeedback(Text.translatable("rollback.command.backupNow.success"), true);
        return 1;
    }

    public static int deleteOldestBackup(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded) server).getBackupManager();
        LevelStorage.Session session = ((MinecraftServerExpanded) server).getSession();
        String worldName = session.getLevelSummary().getName();

        backupManager.deleteOldestBackup(worldName);

        context.getSource().sendFeedback(Text.translatable("rollback.command.backupDelete.success"), true);
        return 1;
    }

    public static int deleteLatestBackup(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded) server).getBackupManager();
        LevelStorage.Session session = ((MinecraftServerExpanded) server).getSession();
        String worldName = session.getLevelSummary().getName();

        backupManager.deleteLatestBackup(worldName);

        context.getSource().sendFeedback(Text.translatable("rollback.command.backupDelete.success"), true);
        return 1;
    }

    public static int listBackups(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        BackupManager backupManager = ((MinecraftServerExpanded) server).getBackupManager();
        String worldName = ((MinecraftServerExpanded) server).getSession().getLevelSummary().getName();
        List<RollbackBackup> backups = backupManager.getRollbacksFor(worldName);

        context.getSource().sendMessage(Text.translatable("rollback.command.list.title"));
        for (int i = 1; i <= backups.size(); i++) {
            RollbackBackup backup = backups.get(backups.size()-i);
            MutableText part1, part2, part3;
            part1 = Text.literal(String.format("    #%-2d    ", i));
            part2 = Text.translatable("rollback.command.list.created", backup.getDateAsString());
            part3 = Text.translatable("rollback.command.list.day", backup.daysPlayed);
            context.getSource().sendMessage(part1.append(part2.append(part3)));
        }

        return 1;
    }
}
