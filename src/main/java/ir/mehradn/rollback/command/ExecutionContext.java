package ir.mehradn.rollback.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ir.mehradn.rollback.command.argument.CommandArgument;
import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public final class ExecutionContext implements HasBuildContext {
    private final Map<String, Object> buildContext;
    private final Map<String, CommandArgument<?>> arguments;
    private final CommandContext<CommandSourceStack> commandContext;

    private ExecutionContext(Map<String, Object> buildContext, Map<String, CommandArgument<?>> arguments,
                             CommandContext<CommandSourceStack> commandContext) {
        this.buildContext = buildContext;
        this.arguments = arguments;
        this.commandContext = commandContext;
    }

    public CommandContext<CommandSourceStack> getCommandContext() {
        return this.commandContext;
    }

    public CommandSourceStack getSource() {
        return this.commandContext.getSource();
    }

    public CommonBackupManager getBackupManager() {
        return ((MinecraftServerExpanded)getSource().getServer()).getBackupManager();
    }

    @SuppressWarnings("unchecked")
    public <T, S extends CommandArgument<T>> @Nullable T getArgument(String name) throws CommandSyntaxException {
        S argument = (S)this.arguments.get(name);
        return argument.get(this, name);
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T getContext(String key) {
        return (T)this.buildContext.get(key);
    }

    public static final class Builder {
        private final Map<String, Object> context;
        private final Map<String, CommandArgument<?>> arguments;

        public Builder(BuildContext buildContext) {
            this.context = buildContext.copyContext();
            this.arguments = buildContext.copyArguments();
        }

        public ExecutionContext build(CommandContext<CommandSourceStack> commandContext) {
            return new ExecutionContext(this.context, this.arguments, commandContext);
        }
    }
}
