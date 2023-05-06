package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import net.minecraft.commands.CommandSourceStack;

public final class ConditionalNode extends CommandNode {
    private final Condition condition;

    public ConditionalNode(Condition condition) {
        this.condition = condition;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        if (this.condition.passes(context))
            command = buildNext(command, context);
        return getNext().build(command, context);
    }

    protected CommandNode getNext() {
        assert this.next != null;
        return this.next.getNext();
    }

    public interface Condition {
        boolean passes(BuildContext context);
    }
}
