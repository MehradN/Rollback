package ir.mehradn.rollback.util.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface EditWorldScreenExpanded {
    BooleanConsumer getCallback();

    void setCallbackAction(Consumer<WorldSelectionListCallbackAction> consumer);
}
