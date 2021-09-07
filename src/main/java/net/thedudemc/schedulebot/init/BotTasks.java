package net.thedudemc.schedulebot.init;

import net.thedudemc.schedulebot.task.MessageExecutorTask;
import net.thedudemc.schedulebot.task.SchedulerTask;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BotTasks {

    private static final List<SchedulerTask> TASKS = new ArrayList<>();

    public static void register() {
        register(new MessageExecutorTask(60000, MILLISECONDS));

        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(TASKS.size());
        TASKS.forEach(task -> schedule.scheduleAtFixedRate(
                task,
                getMillisecondsToNextMinute(),
                task.getInterval(),
                task.getUnits())
        );
    }

    private static void register(SchedulerTask task) {
        TASKS.add(task);
    }

    private static long getMillisecondsToNextMinute() {
        return Instant
                .now()
                .plus(1, ChronoUnit.MINUTES)
                .truncatedTo(ChronoUnit.MINUTES)
                .minusMillis(Instant.now().toEpochMilli())
                .toEpochMilli();
    }
}
