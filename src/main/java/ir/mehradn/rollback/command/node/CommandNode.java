package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import net.minecraft.commands.CommandSourceStack;

public abstract class CommandNode {
    protected CommandNode next = null;

    public abstract ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context);

    protected ArgumentBuilder<CommandSourceStack, ?> buildNext(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        assert this.next != null;
        return this.next.build(command, context);
    }

    protected CommandNode getNext() {
        assert this.next != null;
        return this.next;
    }

    public void setNext(CommandNode next) {
        this.next = next;
    }
}
