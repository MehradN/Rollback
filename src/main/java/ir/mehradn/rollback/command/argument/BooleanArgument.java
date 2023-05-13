package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.HasBuildContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;

public class BooleanArgument extends CommandArgument<Boolean> {
    private static final String BOOL = ":bool";

    public @Nullable Boolean get(HasBuildContext context, String name) {
        if (ArgumentNode.exists(context, name))
            return context.getContext(name + BOOL);
        return this.defaultValue;
    }

    @Override
    public @Nullable Boolean get(ExecutionContext context, String name) throws CommandSyntaxException {
        return get((HasBuildContext)context, name);
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next, BuildContext context) {
        context.set(name + BOOL, false);
        command.then(next.build(Commands.literal("false"), context));
        context.set(name + BOOL, true);
        command.then(next.build(Commands.literal("true"), context));
        context.remove(name + BOOL);
    }
}
