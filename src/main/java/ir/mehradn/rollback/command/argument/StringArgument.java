package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;

public final class StringArgument extends CommandArgument<String> {
    private final Validator validator;

    public StringArgument() {
        this((str) -> { });
    }

    public StringArgument(Validator validator) {
        this.validator = validator;
    }

    @Override
    public @Nullable String get(ExecutionContext context, String name) throws CommandSyntaxException {
        String string = getStringArgument(context, name);
        if (string == null)
            return this.defaultValue;
        this.validator.validate(string);
        return string;
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                      BuildContext context) {
        command.then(next.build(Commands.argument(name, StringArgumentType.string()), context));
    }

    private static @Nullable String getStringArgument(ExecutionContext context, String name) {
        if (!ArgumentNode.exists(context, name))
            return null;
        String value = StringArgumentType.getString(context.getCommandContext(), name).strip();
        if (value.isBlank())
            return null;
        return value;
    }

    @FunctionalInterface
    public interface Validator {
        void validate(String value) throws CommandSyntaxException;
    }
}
