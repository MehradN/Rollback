package ir.mehradn.rollback.command;

import ir.mehradn.rollback.command.argument.CommandArgument;
import java.util.HashMap;

public final class BuildContext implements HasBuildContext {
    private final HashMap<String, Object> context;
    private final HashMap<String, CommandArgument<?>> arguments;

    public BuildContext() {
        this.context = new HashMap<>();
        this.arguments = new HashMap<>();
    }

    public void set(String key, Object value) {
        this.context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getContext(String key) {
        return (T)this.context.get(key);
    }

    public void remove(String key) {
        this.context.remove(key);
    }

    public void addArgument(String key, CommandArgument<?> argument) {
        this.arguments.put(key, argument);
    }

    @SuppressWarnings("unchecked")
    public <T extends CommandArgument<?>> T getArgument(String key) {
        return (T)this.arguments.get(key);
    }

    HashMap<String, Object> copyContext() {
        return new HashMap<>(this.context);
    }

    HashMap<String, CommandArgument<?>> copyArguments() {
        return this.arguments;
    }
}
