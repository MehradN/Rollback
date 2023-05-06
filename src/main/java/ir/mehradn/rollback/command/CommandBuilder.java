package ir.mehradn.rollback.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.argument.CommandArgument;
import ir.mehradn.rollback.command.node.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import java.util.ArrayList;

public final class CommandBuilder {
    private final String name;
    private final ArrayList<CommandNode> nodes;
    private final BuildContext context;

    public CommandBuilder(String name) {
        this.name = name;
        this.nodes = new ArrayList<>();
        this.context = new BuildContext();
    }

    public ArgumentBuilder<CommandSourceStack, ?> build() {
        CommandNode node = this.nodes.get(0);
        return node.build(Commands.literal(this.name), this.context);
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
