package ir.mehradn.rollback.event;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.CommandBuilder;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.argument.*;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
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
                .argument("name", new StringArgument(Lambdas::nameRequirement))
                .executes(RollbackCommand::createBackup);
            CommandBuilder command2 = new CommandBuilder("create-manual")
                .executes(RollbackCommand::createManualBackup);
            CommandBuilder command3 = new CommandBuilder("delete")
                .argument("type", new EnumArgument<>(BackupType.class, (type) -> type.manualDeletion))
                .argument("index", new IndexArgument((ctx) -> Lambdas.getIDList(ctx, "type"),
                    (ctx) -> Lambdas.getMaxCount(ctx, "type")))
                .executes(RollbackCommand::deleteBackup);
            CommandBuilder command4 = new CommandBuilder("convert")
                .argument("from", new EnumArgument<>(BackupType.class, (type) -> type.convertFrom))
                .argument("index", new IndexArgument((ctx) -> Lambdas.getIDList(ctx, "from"),
                    (ctx) -> Lambdas.getMaxCount(ctx, "from")))
                .argument("to", new EnumArgument<>(BackupType.class,
                    (ctx, type) -> type.convertTo && type != Lambdas.getType(ctx, "from")))
                .conditional((ctx) -> Lambdas.getType(ctx, "to").list)
                .argument("name", new StringArgument(Lambdas::nameRequirement))
                .executes(RollbackCommand::convertBackup);
            CommandBuilder command5 = new CommandBuilder("list")
                .argument("type", new EnumArgument<>(BackupType.class, (type) -> type.list))
                .conditional((ctx) -> Lambdas.getMaxCount(ctx, "type") > DEFAULT_COUNT)
                .argument("count", new IntegerArgument((ctx) -> 1, (ctx) -> Lambdas.getMaxCount(ctx, "type")).setDefault(DEFAULT_COUNT))
                .executes(RollbackCommand::listBackup);

            dispatcher.register(Commands.literal("rollback")
                .requires(Lambdas::hasRequirements)
                .then(command1.build())
                .then(command2.build())
                .then(command3.build())
                .then(command4.build())
                .then(command5.build())
                .then(ConfigCommand.build()));
        });
    }

    private static int createBackup(ExecutionContext context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();
        String name = context.<String, StringArgument>getArgument("name");

        try {
            backupManager.createBackup(name, BackupType.COMMAND);
            source.sendSuccess(Component.translatable("rollback.command.create.success.command"), true);
            return 1;
        } catch (BackupManagerException e) {
            return 0;
        }
    }

    private static int createManualBackup(ExecutionContext context) {
        CommandSourceStack source = context.getSource();
        CommonBackupManager backupManager = context.getBackupManager();

        try {
            backupManager.createBackup(null, BackupType.MANUAL);
            source.sendSuccess(Component.translatable("rollback.command.create.success.manual"), true);
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

    private static class ConfigCommand {
        public static ArgumentBuilder<CommandSourceStack, ?> build() {
            ArgumentBuilder<CommandSourceStack, ?> command = Commands.literal("config");
            for (RollbackWorldConfig.Entry entry : RollbackWorldConfig.Entry.values()) {
                CommandBuilder c1 = new CommandBuilder(entry.name)
                    .optional()
                    .argument("value", entry.commandArgument)
                    .executes((ctx) -> entrySetOrGet(ctx, entry));
                command.then(c1.build());
            }
            CommandBuilder c2 = new CommandBuilder("reset")
                .argument("entry", new EnumArgument<>(RollbackWorldConfig.Entry.class))
                .executes(ConfigCommand::entryReset);
            CommandBuilder c3 = new CommandBuilder("save-as-default")
                .executes(ConfigCommand::saveAsDefault);
            return command
                .then(c2.build())
                .then(c3.build());
        }

        private static <T, S extends CommandArgument<T>> int entrySetOrGet(ExecutionContext context, RollbackWorldConfig.Entry entry)
            throws CommandSyntaxException {
            CommonBackupManager backupManager = context.getBackupManager();
            ConfigEntry<T> configEntry = backupManager.getWorld().config.getEntry(entry);

            if (ArgumentNode.exists(context, "value")) {
                T value = context.<T, S>getArgument("value");
                Assertion.argument(value != null);
                configEntry.set(value);
                try {
                    backupManager.saveConfig();
                    return 1;
                } catch (BackupManagerException e) {
                    return 0;
                }
            }

            String key = (configEntry.needsFallback() ? "rollback.command.config.entry.default" : "rollback.command.config.entry");
            context.getSource().sendSystemMessage(Component.translatable(key, configEntry.name, configEntry.getAsString()));
            return 1;
        }

        private static <T> int entryReset(ExecutionContext context) throws CommandSyntaxException {
            RollbackWorldConfig.Entry entry = context.<RollbackWorldConfig.Entry, EnumArgument<RollbackWorldConfig.Entry>>getArgument("entry");
            Assertion.argument(entry != null);

            CommonBackupManager backupManager = context.getBackupManager();
            ConfigEntry<T> configEntry = backupManager.getWorld().config.getEntry(entry);
            configEntry.reset();
            try {
                backupManager.saveConfig();
                return 1;
            } catch (BackupManagerException e) {
                return 0;
            }
        }

        private static int saveAsDefault(ExecutionContext context) {
            CommonBackupManager backupManager = context.getBackupManager();
            try {
                backupManager.saveConfigAsDefault();
                return 1;
            } catch (BackupManagerException e) {
                return 0;
            }
        }
    }
}
