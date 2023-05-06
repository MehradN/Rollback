package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.CommandNode;
import ir.mehradn.rollback.rollback.BackupManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class NameArgument extends CommandArgument<String> {
    public String get(ExecutionContext context, String name) throws CommandSyntaxException {
        String string = getStringArgument(context, name);
        if (string == null)
            return this.defaultValue;
        if (string.length() > BackupManager.MAX_NAME_LENGTH)
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.nameTooLong", BackupManager.MAX_NAME_LENGTH)).create();
        return string;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                                                        BuildContext context) {
        return command.then(next.build(Commands.argument(name, StringArgumentType.string()), context));
    }
}
