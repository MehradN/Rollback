package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;

public abstract class CommandArgument <T> {
    protected T defaultValue = null;

    public static String getStringArgument(ExecutionContext context, String name) {
        if (!ArgumentNode.exists(context, name))
            return null;
        String value = StringArgumentType.getString(context.getCommandContext(), name).strip();
        if (value.isBlank())
            return null;
        return value;
    }

    public CommandArgument<T> setDefault(T value) {
        this.defaultValue = value;
        return this;
    }

    public abstract T get(ExecutionContext context, String name) throws CommandSyntaxException;

    public abstract ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                                                                 BuildContext context);

    public interface ContextAware <S> {
        S get(BuildContext context);
    }
}
