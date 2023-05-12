package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public final class IndexArgument extends CommandArgument<Integer> {
    private static final String INDEX = ":index";
    private final IDListBuilder idListBuilder;
    private final ContextAware<Integer> maxIndex;

    public IndexArgument(IDListBuilder idListBuilder, int maxIndex) {
        this(idListBuilder, (ctx) -> maxIndex);
    }

    public IndexArgument(IDListBuilder idListBuilder, ContextAware<Integer> maxIndex) {
        this.idListBuilder = idListBuilder;
        this.maxIndex = maxIndex;
    }

    @Override
    public @Nullable Integer get(ExecutionContext context, String name) throws CommandSyntaxException {
        int index;
        if (!ArgumentNode.exists(context, name)) {
            Assertion.state(this.defaultValue != null, "A default value is required!");
            index = -this.defaultValue;
        } else {
            IndexType type = context.getContext(name + INDEX);
            switch (type) {
                case LATEST -> index = -1;
                case NUMBER -> index = -IntegerArgumentType.getInteger(context.getCommandContext(), name);
                default -> index = 0; // OLDEST
            }
        }

        List<Integer> ids = this.idListBuilder.build(context);
        if (ids.isEmpty() || index < -ids.size())
            throw new SimpleCommandExceptionType(Component.translatable("rollback.command.invalidIndex")).create();
        if (index < 0)
            index += ids.size();

        Collections.sort(ids);
        return ids.get(index);
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                      BuildContext context) {
        context.set(name + INDEX, IndexType.OLDEST);
        command.then(next.build(Commands.literal("oldest"), context));
        context.set(name + INDEX, IndexType.LATEST);
        command.then(next.build(Commands.literal("latest"), context));
        context.set(name + INDEX, IndexType.NUMBER);
        command.then(next.build(Commands.argument(name, IntegerArgumentType.integer(1, this.maxIndex.get(context))), context));
        context.remove(name + INDEX);
    }

    @FunctionalInterface
    public interface IDListBuilder {
        List<Integer> build(ExecutionContext context) throws CommandSyntaxException;
    }

    private enum IndexType {
        OLDEST,
        LATEST,
        NUMBER
    }
}
