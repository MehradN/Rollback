package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public abstract class SkipperNode extends CommandNode {
    @Override
    public void setNext(@NotNull CommandNode next) {
        Assertion.argument(!(next instanceof SkipperNode), "Chaining skipper nodes isn't allowed!");
        super.setNext(next);
    }

    protected ArgumentBuilder<CommandSourceStack, ?> buildNextNext(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        return this.getNext().buildNext(command, context);
    }
}
