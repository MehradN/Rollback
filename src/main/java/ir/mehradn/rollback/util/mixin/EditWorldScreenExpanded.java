package ir.mehradn.rollback.util.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.Consumer;

public interface EditWorldScreenExpanded {
    BooleanConsumer getCallback();

    void setCallbackAction(Consumer<WorldSelectionListCallbackAction> consumer);
}
