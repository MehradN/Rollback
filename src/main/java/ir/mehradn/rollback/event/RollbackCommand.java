package ir.mehradn.rollback.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.CommandBuilder;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.argument.EnumArgument;
import ir.mehradn.rollback.command.argument.IndexArgument;
import ir.mehradn.rollback.command.argument.IntegerArgument;
import ir.mehradn.rollback.command.argument.StringArgument;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");

            CommandBuilder command1 = new CommandBuilder("create")
                .optional()
                .argument("type", new EnumArgument<>(BackupType.class, (type) -> type.manualCreation).setDefault(BackupType.COMMAND))
                .conditional((ctx) -> Lambdas.getType(ctx, "type").list)
                .argument("name", new StringArgument(Lambdas::nameRequirement))
                .executes(RollbackCommand::createBackup);
            CommandBuilder command2 = new CommandBuilder("delete")
                .argument("type", new EnumArgument<>(BackupType.class, (type) -> type.manualDeletion))
                .argument("index", new IndexArgument((ctx) -> Lambdas.getIDList(ctx, "type"),
                    (ctx) -> Lambdas.getMaxCount(ctx, "type")))
                .executes(RollbackCommand::deleteBackup);
            CommandBuilder command3 = new CommandBuilder("convert")
                .argument("from", new EnumArgument<>(BackupType.class, (type) -> type.convertFrom))
                .argument("index", new IndexArgument((ctx) -> Lambdas.getIDList(ctx, "from"),
                    (ctx) -> Lambdas.getMaxCount(ctx, "from")))
                .argument("to", new EnumArgument<>(BackupType.class,
                    (ctx, type) -> type.convertTo && type != Lambdas.getType(ctx, "from")))
                .conditional((ctx) -> Lambdas.getType(ctx, "to").list)
                .argument("name", new StringArgument(Lambdas::nameRequirement))
                .executes(RollbackCommand::convertBackup);
            CommandBuilder command4 = new CommandBuilder("list")
                .argument("type", new EnumArgument<>(BackupType.class, (type) -> type.list))
                .conditional((ctx) -> Lambdas.getMaxCount(ctx, "type") > DEFAULT_COUNT)
                .argument("count", new IntegerArgument((ctx) -> 1, (ctx) -> Lambdas.getMaxCount(ctx, "type")).setDefault(DEFAULT_COUNT))
                .executes(RollbackCommand::listBackup);

            dispatcher.register(Commands.literal("rollback")
                .requires(Lambdas::hasRequirements)
                .then(command1.build())
                .then(command2.build())
                .then(command3.build())
                .then(command4.build()));
        });
    }

    private static int createBackup(ExecutionContext context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType type = context.<BackupType, EnumArgument<BackupType>>getArgument("type");
        String name = context.<String, StringArgument>getArgument("name");
        Assertion.argument(type != null && type.manualCreation);

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
        BackupType type = context.<BackupType, EnumArgument<BackupType>>getArgument("type");
        Integer id = context.<Integer, IndexArgument>getArgument("index");
        Assertion.argument(id != null && type != null && type.manualDeletion);

        try {
            backupManager.deleteBackup(id, type);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int convertBackup(ExecutionContext context) throws CommandSyntaxException {
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType from = context.<BackupType, EnumArgument<BackupType>>getArgument("from");
        Integer id = context.<Integer, IndexArgument>getArgument("index");
        BackupType to = context.<BackupType, EnumArgument<BackupType>>getArgument("to");
        String name = context.<String, StringArgument>getArgument("name");
        Assertion.argument(id != null && from != null && to != null && from != to && from.convertFrom && to.convertTo);

        try {
            backupManager.convertBackup(id, from, name, to);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int listBackup(ExecutionContext context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();
        BackupType type = context.<BackupType, EnumArgument<BackupType>>getArgument("type");
        Integer count = context.<Integer, IntegerArgument>getArgument("count");
        Assertion.argument(count != null && type != null && type.list);

        List<RollbackBackup> backups = new ArrayList<>(new TreeMap<>(backupManager.getWorld().getBackups(type)).values());
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

    private static class Lambdas {
        public static boolean hasRequirements(CommandSourceStack source) {
            return (source.getPlayer() != null && source.hasPermission(4));
        }

        public static void nameRequirement(String name) throws CommandSyntaxException {
            if (name.length() > BackupManager.MAX_NAME_LENGTH)
                throw new SimpleCommandExceptionType(Component.translatable("rollback.command.nameTooLong", BackupManager.MAX_NAME_LENGTH))
                    .create();
        }

        public static BackupType getType(BuildContext context, String name) {
            EnumArgument<BackupType> argument = context.getArgument(name);
            BackupType type = argument.get(context, name);
            Assertion.argument(type != null);
            return type;
        }

        public static int getMaxCount(BuildContext context, String name) {
            BackupType type = getType(context, name);
            return RollbackConfig.getMaxMaxBackups(type);
        }

        public static List<Integer> getIDList(ExecutionContext context, String name) throws CommandSyntaxException {
            BackupType type = context.<BackupType, EnumArgument<BackupType>>getArgument(name);
            CommonBackupManager backupManager = context.getBackupManager();
            Assertion.argument(type != null);
            return new ArrayList<>(backupManager.getWorld().getBackups(type).keySet());
        }
    }
}
