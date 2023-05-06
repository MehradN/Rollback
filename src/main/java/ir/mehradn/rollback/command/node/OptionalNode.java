package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import net.minecraft.commands.CommandSourceStack;

public final class OptionalNode extends CommandNode {
    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        return getNext().build(buildNext(command, context), context);
    }

    protected CommandNode getNext() {
        assert this.next != null;
        return this.next.getNext();
    }
}
