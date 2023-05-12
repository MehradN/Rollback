package ir.mehradn.rollback.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.argument.CommandArgument;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import ir.mehradn.rollback.command.node.ExecutionNode;
import ir.mehradn.rollback.command.node.LiteralNode;
import ir.mehradn.rollback.command.node.skipper.ConditionalNode;
import ir.mehradn.rollback.command.node.skipper.OptionalNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import java.util.ArrayList;

public final class CommandBuilder {
    private final String name;
    private final ArrayList<CommandNode> nodes = new ArrayList<>();
    private final BuildContext context = new BuildContext();

    public CommandBuilder(String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build() {
        ArgumentBuilder<CommandSourceStack, ?> command = Commands.literal(this.name);
        if (this.nodes.isEmpty())
            return command;
        CommandNode node = this.nodes.get(0);
        return node.build(command, this.context);
    }

    public CommandBuilder then(CommandNode node) {
        if (!this.nodes.isEmpty())
            this.nodes.get(this.nodes.size() - 1).setNext(node);
        this.nodes.add(node);
        return this;
    }

    public CommandBuilder argument(String name, CommandArgument<?> argument) {
        ArgumentNode node = new ArgumentNode(name, argument);
        node.addArgument(this.context);
        return then(node);
    }

    public CommandBuilder optional() {
        return then(new OptionalNode());
    }

    public CommandBuilder conditional(ConditionalNode.Condition condition) {
        return then(new ConditionalNode(condition));
    }

    public CommandBuilder literal(String text) {
        return then(new LiteralNode(text));
    }

    public CommandBuilder executes(ExecutionNode.Executable action) {
        return then(new ExecutionNode(action));
    }
}
