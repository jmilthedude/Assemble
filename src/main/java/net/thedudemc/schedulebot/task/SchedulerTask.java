package net.thedudemc.schedulebot.task;

import net.thedudemc.schedulebot.ScheduleBot;

import java.util.concurrent.TimeUnit;

public abstract class SchedulerTask implements Runnable {

    private final int interval;
    private final TimeUnit units;

    public SchedulerTask(int interval, TimeUnit units) {
        this.interval = interval;
        this.units = units;
    }

    public int getInterval() {
        return interval;
    }

    public TimeUnit getUnits() {
        return units;
    }
}
