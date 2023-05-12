package ir.mehradn.rollback.command.node.skipper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.node.SkipperNode;
import net.minecraft.commands.CommandSourceStack;

public final class OptionalNode extends SkipperNode {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        return buildNextNext(buildNext(command, context), context);
    }
}
