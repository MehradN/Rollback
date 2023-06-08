package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashSet;

public final class OpenGUI implements FabricPacket {
    public static final PacketType<OpenGUI> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "open_gui"),
        OpenGUI::new
    );
    public static final HashSet<ServerPlayer> awaitingPlayers = new HashSet<>();

    public OpenGUI() { }

    public OpenGUI(FriendlyByteBuf buf) { }

    @Override
    public void write(FriendlyByteBuf buf) { }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
