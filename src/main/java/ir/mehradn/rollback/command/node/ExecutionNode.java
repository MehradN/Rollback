package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import net.minecraft.commands.CommandSourceStack;

public final class ExecutionNode extends CommandNode {
    private final Executable action;

    public ExecutionNode(Executable action) {
        this.action = action;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        ExecutionContext.Builder builder = new ExecutionContext.Builder(context);
        return command.executes((ctx) -> this.action.execute(builder.build(ctx)));
    }

    @FunctionalInterface
    public interface Executable {
        int execute(ExecutionContext context) throws CommandSyntaxException;
    }
}
