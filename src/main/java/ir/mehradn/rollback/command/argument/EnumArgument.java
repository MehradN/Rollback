package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.HasBuildContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;

public final class EnumArgument <T extends Enum<T>> extends CommandArgument<T> {
    private static final String TYPE = ":type";
    private final Class<T> enumClass;
    private final ContextAwareTypeFilter<T> enumFilter;

    public EnumArgument(Class<T> enumClass) {
        this(enumClass, (ctx, value) -> true);
    }

    public EnumArgument(Class<T> enumClass, TypeFilter<T> enumFilter) {
        this(enumClass, (ctx, value) -> enumFilter.accepts(value));
    }

    public EnumArgument(Class<T> enumClass, ContextAwareTypeFilter<T> enumFilter) {
        this.enumClass = enumClass;
        this.enumFilter = enumFilter;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumArgument<T> createEnumArgument(Class<?> enumClass) {
        Assertion.argument(enumClass.isEnum(), "The given class isn't an enum!");
        return new EnumArgument<>((Class<T>)enumClass);
    }

    public @Nullable T get(HasBuildContext context, String name) {
        if (ArgumentNode.exists(context, name))
            return context.getContext(name + TYPE);
        return this.defaultValue;
    }

    @Override
    public @Nullable T get(ExecutionContext context, String name) {
        return get((HasBuildContext)context, name);
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                      BuildContext context) {
        for (T value : this.enumClass.getEnumConstants()) {
            context.set(name + TYPE, value);
            if (this.enumFilter.accepts(context, value))
                command.then(next.build(Commands.literal(value.toString()), context));
        }
        context.remove(name + TYPE);
    }

    @FunctionalInterface
    public interface TypeFilter <T> {
        boolean accepts(T value);
    }

    @FunctionalInterface
    public interface ContextAwareTypeFilter <T> {
        boolean accepts(BuildContext context, T value);
    }
}