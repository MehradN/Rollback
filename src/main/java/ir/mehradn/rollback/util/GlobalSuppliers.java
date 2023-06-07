package ir.mehradn.rollback.util;

import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.exception.Assertion;
import java.util.function.Supplier;

public final class GlobalSuppliers {
    private static Supplier<RollbackDefaultConfig> defaultConfigSupplier = null;

    public static void setDefaultConfigSupplier(Supplier<RollbackDefaultConfig> supplier) {
        defaultConfigSupplier = supplier;
    }

    public static RollbackDefaultConfig buildDefaultConfig() {
        Assertion.state(defaultConfigSupplier != null, "setDefaultConfigSupplier must be called at all entrypoints!");
        return defaultConfigSupplier.get();
    }
}
