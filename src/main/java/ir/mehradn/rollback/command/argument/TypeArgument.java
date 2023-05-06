package ir.mehradn.rollback.command.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.BuildContext;
import ir.mehradn.rollback.command.ExecutionContext;
import ir.mehradn.rollback.command.HasBuildContext;
import ir.mehradn.rollback.command.node.ArgumentNode;
import ir.mehradn.rollback.command.node.CommandNode;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class TypeArgument extends CommandArgument<BackupType> {
    private static final String TYPE = ":type";
    private final ContextAwareTypeFilter typeFilter;

    public TypeArgument() {
        this((ctx, type) -> true);
    }

    public TypeArgument(TypeFilter typeFilter) {
        this((ctx, type) -> typeFilter.accepts(type));
    }

    public TypeArgument(ContextAwareTypeFilter typeFilter) {
        this.typeFilter = typeFilter;
    }

    public BackupType get(ExecutionContext context, String name) throws CommandSyntaxException {
        return get((HasBuildContext)context, name);
    }

    public BackupType get(HasBuildContext context, String name) {
        if (ArgumentNode.exists(context, name))
            return context.getContext(name + TYPE);
        return this.defaultValue;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> command, String name, CommandNode next,
                                                        BuildContext context) {
        for (BackupType type : BackupType.values()) {
            context.set(name + TYPE, type);
            if (this.typeFilter.accepts(context, type))
                command = command.then(next.build(Commands.literal(type.toString()), context));
        }
        context.remove(name + TYPE);
        return command;
    }

    public interface TypeFilter {
        boolean accepts(BackupType type);
    }

    public interface ContextAwareTypeFilter {
        boolean accepts(BuildContext context, BackupType type);
    }
}