package ir.mehradn.rollback.util;

public class TickTimer {
    private final Runnable onTimeout;
    private int tillTimeout;

    public TickTimer(int ticks, Runnable onTimeout) {
        this.onTimeout = onTimeout;
        this.tillTimeout = ticks;
    }

    public void tick() {
        if (this.tillTimeout < 0)
            return;
        this.tillTimeout--;
        if (this.tillTimeout == 0)
            this.onTimeout.run();
    }

    public boolean isDone() {
        return (this.tillTimeout <= 0);
    }
}
