package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CountArgument extends CommandArgument<Integer> {
    private final ContextAware<Integer> maxCount;

    public CountArgument(int maxCount) {
        this((ctx) -> maxCount);
    }

    public CountArgument(ContextAware<Integer> maxCount) {
        this.maxCount = maxCount;
    }

    public Integer get(ExecutionContext context, String name) throws CommandSyntaxException {
        if (ArgumentNode.exists(context, name))
            return IntegerArgumentType.getInteger(context.getCommandContext(), name);
        return this.defaultValue;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                                                        BuildContext context) {
        return command.then(next.build(Commands.argument(name, IntegerArgumentType.integer(1, this.maxCount.get(context))), context));
    }
}
