package ir.mehradn.rollback.event;

import ir.mehradn.rollback.util.TickTimer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import java.util.Iterator;
import java.util.LinkedList;

public final class ServerTickTimer {
    private static final LinkedList<TickTimer> timers = new LinkedList<>();

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(ServerTickTimer::onStartTick);
    }

    public static void addTimer(TickTimer timer) {
        timers.add(timer);
    }

    private static void onStartTick(MinecraftServer server) {
        for (Iterator<TickTimer> it = timers.iterator(); it.hasNext(); ) {
            TickTimer timer = it.next();
            timer.tick();
            if (timer.isDone())
                it.remove();
        }
    }
}
