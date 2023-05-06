package ir.mehradn.rollback.command.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.HasBuildContext;
import ir.mehradn.rollback.command.argument.CommandArgument;
import net.minecraft.commands.CommandSourceStack;

public final class ArgumentNode extends CommandNode {
    private static final String EXISTS = ":exists";
    private final String name;
    private final CommandArgument<?> argument;

    public ArgumentNode(String name, CommandArgument<?> argument) {
        this.name = name;
        this.argument = argument;
    }

    public static boolean exists(HasBuildContext context, String name) {
        Boolean b = context.getContext(name + EXISTS);
        return b != null && b;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, BuildContext context) {
        context.set(this.name + EXISTS, true);
        command = this.argument.build(command, this.name, getNext(), context);
        context.remove(this.name + EXISTS);
        return command;
    }

    public void addArgument(BuildContext context) {
        context.addArgument(this.name, this.argument);
    }
}
