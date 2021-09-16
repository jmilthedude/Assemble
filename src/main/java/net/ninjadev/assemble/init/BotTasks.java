package net.ninjadev.assemble.init;

import net.ninjadev.assemble.task.SchedulerTask;
import net.ninjadev.assemble.task.MessageExecutorTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BotTasks {

    private static final List<SchedulerTask> TASKS = new ArrayList<>();
    private static final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(16);

    public static void register() {
        register(new MessageExecutorTask(60000, MILLISECONDS));
    }

    private static void register(SchedulerTask task) {
        schedule.scheduleAtFixedRate(
                task,
                getMillisecondsToNextMinute(),
                task.getInterval(),
                task.getUnits());

        TASKS.add(task);
    }

    private static long getMillisecondsToNextMinute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMinute = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        Duration duration = Duration.between(now, nextMinute);
        return duration.toMillis();
    }
}
