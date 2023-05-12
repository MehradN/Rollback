package ir.mehradn.rollback.command.node.skipper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.node.SkipperNode;
import net.minecraft.commands.CommandSourceStack;

public final class ConditionalNode extends SkipperNode {
    private final Condition condition;

    public ConditionalNode(Condition condition) {
        this.condition = condition;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        if (this.condition.passes(context))
            buildNext(command, context);
        return buildNextNext(command, context);
    }

    @FunctionalInterface
    public interface Condition {
        boolean passes(BuildContext context);
    }
}
