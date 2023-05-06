package ir.mehradn.rollback.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.CommandBuilder;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.argument.CountArgument;
import ir.mehradn.rollback.command.argument.IndexArgument;
import ir.mehradn.rollback.command.argument.NameArgument;
import ir.mehradn.rollback.command.argument.TypeArgument;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.exception.BackupIOException;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RollbackCommand {
    private static final int DEFAULT_COUNT = 10;

    public static void register() {
        CommandBuilder command1 = new CommandBuilder("create")
            .optional()
            .argument("type", new TypeArgument((type) -> type.manualCreation).setDefault(BackupType.COMMAND))
            .conditional((ctx) -> getType(ctx, "type").list)
            .argument("name", new NameArgument())
            .executes(RollbackCommand::createBackup);
        CommandBuilder command2 = new CommandBuilder("delete")
            .argument("type", new TypeArgument((type) -> type.manualDeletion))
            .argument("index", new IndexArgument((ctx) -> getIDList(ctx, "type"), (ctx) -> getMaxCount(ctx, "type")))
            .executes(RollbackCommand::deleteBackup);
        CommandBuilder command3 = new CommandBuilder("convert")
            .argument("from", new TypeArgument((type) -> type.convertFrom))
            .argument("index", new IndexArgument((ctx) -> getIDList(ctx, "from"), (ctx) -> getMaxCount(ctx, "from")))
            .argument("to", new TypeArgument((ctx, type) -> type.convertTo && type != getType(ctx, "from")))
            .conditional((ctx) -> getType(ctx, "to").list)
            .argument("name", new NameArgument())
            .executes(RollbackCommand::convertBackup);
        CommandBuilder command4 = new CommandBuilder("list")
            .argument("type", new TypeArgument((type) -> type.list))
            .conditional((ctx) -> getMaxCount(ctx, "type") > DEFAULT_COUNT)
            .argument("count", new CountArgument((ctx) -> getMaxCount(ctx, "type")).setDefault(DEFAULT_COUNT))
            .executes(RollbackCommand::listBackup);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            dispatcher.register(Commands.literal("rollback")
                .requires(RollbackCommand::hasRequirements)
                .then(command1.build())
                .then(command2.build())
                .then(command3.build())
                .then(command4.build()));
        });
    }

    private static boolean hasRequirements(CommandSourceStack source) {
        return (source.getPlayer() != null && source.hasPermission(4));
    }

    private static BackupType getType(BuildContext context, String name) {
        TypeArgument argument = context.getArgument(name);
        return argument.get(context, name);
    }

    private static int getMaxCount(BuildContext context, String name) {
        BackupType type = getType(context, name);
        return RollbackConfig.maxBackupsPerWorld(type);
    }

    private static List<Integer> getIDList(ExecutionContext context, String name) throws CommandSyntaxException {
        BackupType type = context.<BackupType, TypeArgument>getArgument(name);
        CommonBackupManager backupManager = context.getBackupManager();
        return new ArrayList<>(backupManager.world.getBackups(type).keySet());
    }

    private static int createBackup(ExecutionContext context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType type = context.<BackupType, TypeArgument>getArgument("type");
        String name = context.<String, NameArgument>getArgument("name");
        assert type.manualCreation;

        try {
            backupManager.createBackup(name, type);
            source.sendSuccess(Component.translatable("rollback.command.create.success." + type), true);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int deleteBackup(ExecutionContext context) throws CommandSyntaxException {
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType type = context.<BackupType, TypeArgument>getArgument("type");
        Integer id = context.<Integer, IndexArgument>getArgument("index");
        assert type.manualDeletion;

        try {
            backupManager.deleteBackup(id, type);
            return 1;
        } catch (BackupIOException e) {
            return 0;
        }
    }

    private static int convertBackup(ExecutionContext context) throws CommandSyntaxException {
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType from = context.<BackupType, TypeArgument>getArgument("from");
        Integer id = context.<Integer, IndexArgument>getArgument("index");
        BackupType to = context.<BackupType, TypeArgument>getArgument("to");
        String name = context.<String, NameArgument>getArgument("name");
        assert from.convertFrom && to.convertTo;

        try {
            backupManager.convertBackup(id, from, name, to);
            return 1;
        } catch (BackupIOException e) {
            return 0;
        }
    }

    private static int listBackup(ExecutionContext context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType type = context.<BackupType, TypeArgument>getArgument("type");
        Integer count = context.<Integer, CountArgument>getArgument("count");
        assert type.list;

        List<RollbackBackup> backups = new ArrayList<>(new TreeMap<>(backupManager.world.getBackups(type)).values());


        if (backups.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.noBackups." + type));
            return 0;
        }

        count = Math.min(backups.size(), count);
        source.sendSystemMessage(Component.translatable("rollback.command.list.title." + type));
        for (int i = 1; i <= count; i++) {
            RollbackBackup backup = backups.get(backups.size() - i);
            Component text;
            String index = String.format("%02d", i);
            String date = backup.getDateAsString();
            String day = backup.getDaysPlayedAsString();
            if (backup.name == null)
                text = Component.translatable("rollback.command.list.item", index, date, day);
            else
                text = Component.translatable("rollback.command.list.itemNamed", index, date, day, backup.name);
            source.sendSystemMessage(text);
        }
        if (count < backups.size())
            source.sendSystemMessage(Component.translatable("rollback.command.list.more"));
        return 1;
    }
}
