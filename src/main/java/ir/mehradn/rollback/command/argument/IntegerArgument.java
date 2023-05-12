package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;

public final class IntegerArgument extends CommandArgument<Integer> {
    private final ContextAware<Integer> min;
    private final ContextAware<Integer> max;

    public IntegerArgument(int min, int max) {
        this((ctx) -> min, (ctx) -> max);
    }

    public IntegerArgument(int min, ContextAware<Integer> max) {
        this((ctx) -> min, max);
    }

    public IntegerArgument(ContextAware<Integer> min, ContextAware<Integer> max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public @Nullable Integer get(ExecutionContext context, String name) {
        if (ArgumentNode.exists(context, name))
            return IntegerArgumentType.getInteger(context.getCommandContext(), name);
        return this.defaultValue;
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                      BuildContext context) {
        command.then(next.build(Commands.argument(name, IntegerArgumentType.integer(this.min.get(context), this.max.get(context))), context));
    }
}
