package net.thedudemc.schedulebot.models;

import java.util.concurrent.TimeUnit;

public class Recurrence {

    private int interval;
    private TimeUnit unit;

    public Recurrence(int interval, TimeUnit unit) {
        this.interval = interval;
        this.unit = unit;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
}
