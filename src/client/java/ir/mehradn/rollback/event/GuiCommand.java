package ir.mehradn.rollback.event;

import com.mojang.brigadier.context.CommandContext;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.util.mixin.MinecraftExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class GuiCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            Rollback.LOGGER.debug("Registering the client rollback command...");
            dispatcher.register(ClientCommandManager.literal("rollback")
                .executes(GuiCommand::openGui));
        }));
    }

    private static int openGui(CommandContext<FabricClientCommandSource> context) {
        Minecraft client = context.getSource().getClient();
        ((MinecraftExpanded)client).runOnNextTick(() -> {
            client.setScreen(null);
            client.pauseGame(false);
            client.setScreen(new RollbackScreen(null, null, client.screen));
        });
        return 1;
    }
}
