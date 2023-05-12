package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommandNode {
    @Nullable protected CommandNode next = null;

    public abstract ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context);

    public void setNext(@NotNull CommandNode next) {
        this.next = next;
    }

    protected ArgumentBuilder<CommandSourceStack, ?> buildNext(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        return this.getNext().build(command, context);
    }

    protected CommandNode getNext() {
        Assertion.state(this.next != null, "Missing next node!");
        return this.next;
    }
}
