package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.node.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommandArgument <T> {
    @Nullable protected T defaultValue = null;

    public CommandArgument<T> setDefault(@NotNull T value) {
        this.defaultValue = value;
        return this;
    }

    public abstract @Nullable T get(ExecutionContext context, String name) throws CommandSyntaxException;

    public abstract void build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next, BuildContext context);

    @FunctionalInterface
    public interface ContextAware <T> {
        T get(BuildContext context);
    }
}
