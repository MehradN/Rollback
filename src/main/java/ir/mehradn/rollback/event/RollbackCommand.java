package ir.mehradn.rollback.event;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class RollbackCommand {
    public static final int MAX_NAME_LENGTH = 32;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Rollback.LOGGER.debug("Registering the rollback command...");
            if (environment.includeIntegrated)
                dispatcher.register(Commands.literal("rollback")
                    .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                            .executes((ctx) -> createBackup(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
                        .executes((ctx) -> createBackup(ctx.getSource(), "")))
                    .then(Commands.literal("delete")
                        .then(Commands.literal("oldest")
                            .executes((ctx) -> deleteBackup(ctx.getSource(), 0)))
                        .then(Commands.literal("latest")
                            .executes((ctx) -> deleteBackup(ctx.getSource(), -1)))
                        .then(Commands.argument("index", IntegerArgumentType.integer(1, 10))
                            .executes((ctx) -> deleteBackup(ctx.getSource(), -IntegerArgumentType.getInteger(ctx, "index")))))
                    .then(Commands.literal("list")
                        .executes((ctx) -> listBackups(ctx.getSource())))
                    .then(configCommand()));
        });
    }

    public static LiteralArgumentBuilder<CommandSourceStack> configCommand() {
        return Commands.literal("config")
            .then(Commands.literal("backupsPerWorld")
                .then(Commands.argument("number", IntegerArgumentType.integer(1, 10))
                    .executes(wrapConfigSetter((ctx) -> RollbackConfig.backupsPerWorld = IntegerArgumentType.getInteger(ctx, "number")))))
            .then(Commands.literal("backupFrequency")
                .then(Commands.argument("number", IntegerArgumentType.integer(1, 15))
                    .executes(wrapConfigSetter((ctx) -> RollbackConfig.backupFrequency = IntegerArgumentType.getInteger(ctx, "number")))))
            .then(Commands.literal("timerMode")
                .then(Commands.literal("daylightCycle")
                    .executes(wrapConfigSetter((ctx) -> RollbackConfig.timerMode = RollbackConfig.TimerMode.DAYLIGHT_CYCLE)))
                .then(Commands.literal("inGameTime")
                    .executes(wrapConfigSetter((ctx) -> RollbackConfig.timerMode = RollbackConfig.TimerMode.IN_GAME_TIME))))
            .then(Commands.literal("automatedBackup")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes((ctx) -> {
                        MinecraftServer server = ctx.getSource().getServer();
                        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
                        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();
                        RollbackWorld rollbackWorld = backupManager.getWorld(worldName);
                        rollbackWorld.automatedBackups = BoolArgumentType.getBool(ctx, "enabled");
                        backupManager.saveMetadata();
                        ctx.getSource().sendSuccess(Component.translatable("rollback.command.config.success"), true);
                        return 0;
                    })));
    }

    public static int createBackup(CommandSourceStack source, String name) {
        if (isNotServerHost(source))
            return -1;

        Rollback.LOGGER.info("Executing the \"backup new\" command...");
        MinecraftServer server = source.getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();

        if (name.isBlank())
            name = null;
        if (name != null && name.length() > MAX_NAME_LENGTH) {
            source.sendFailure(Component.translatable("rollback.command.create.nameTooLong"));
            return -2;
        }

        boolean f = backupManager.createRollbackBackup(server, name);
        if (!f) {
            source.sendFailure(Component.translatable("rollback.createBackup.failed"));
            return 0;
        }

        source.sendSuccess(Component.translatable("rollback.createBackup.success"), true);
        return 1;
    }

    public static int deleteBackup(CommandSourceStack source, int index) {
        if (isNotServerHost(source))
            return -1;

        Rollback.LOGGER.info("Executing the \"backup delete\" command...");
        MinecraftServer server = source.getServer();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        LevelStorageSource.LevelStorageAccess levelAccess = ((MinecraftServerExpanded)server).getLevelAccess();
        String worldName = levelAccess.getLevelId();

        boolean f = backupManager.deleteBackup(worldName, index);
        if (!f) {
            source.sendFailure(Component.translatable("rollback.deleteBackup.failed"));
            return 0;
        }

        source.sendSuccess(Component.translatable("rollback.deleteBackup.success"), true);
        return 1;
    }

    public static int listBackups(CommandSourceStack source) {
        if (isNotServerHost(source))
            return -1;

        MinecraftServer server = source.getServer();
        String worldName = ((MinecraftServerExpanded)server).getLevelAccess().getLevelId();
        BackupManager backupManager = ((MinecraftServerExpanded)server).getBackupManager();
        RollbackWorld world = backupManager.getWorld(worldName);

        if (world.backups.isEmpty()) {
            source.sendSystemMessage(Component.translatable("rollback.command.list.noBackups"));
            return 0;
        }

        source.sendSystemMessage(Component.translatable("rollback.command.list.title"));
        for (int i = 1; i <= world.backups.size(); i++) {
            RollbackBackup backup = world.backups.get(world.backups.size() - i);
            MutableComponent text;
            String index = String.format("%-2d", i);
            String date = backup.getDateAsString();
            String day = backup.getDaysPlayedAsString();
            if (backup.name == null)
                text = Component.translatable("rollback.command.list.item", index, date, day);
            else
                text = Component.translatable("rollback.command.list.itemNamed", index, date, day, backup.name);

            server.sendSystemMessage(text);
        }
        return 1;
    }

    private static Command<CommandSourceStack> wrapConfigSetter(Consumer<CommandContext<CommandSourceStack>> configSetter) {
        return (ctx) -> {
            configSetter.accept(ctx);
            RollbackConfig.write(Rollback.MOD_ID);
            ctx.getSource().sendSuccess(Component.translatable("rollback.command.config.success"), true);
            return 0;
        };
    }

    private static boolean isNotServerHost(CommandSourceStack source) {
        LocalPlayer player1 = Minecraft.getInstance().player;
        ServerPlayer player2 = source.getPlayer();
        if (player1 == null || player2 == null) {
            source.sendFailure(Component.translatable("rollback.command.playerOnly"));
            return true;
        }

        if (!player1.getUUID().equals(player2.getUUID())) {
            source.sendFailure(Component.translatable("rollback.command.unavailable"));
            return true;
        }
        return false;
    }
}
