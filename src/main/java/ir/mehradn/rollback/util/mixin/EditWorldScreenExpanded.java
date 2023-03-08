package ir.mehradn.rollback.util.mixin;

import java.util.function.Consumer;

public interface EditWorldScreenExpanded {
    void setCallbackAction(Consumer<WorldSelectionListCallbackAction> consumer);
}
