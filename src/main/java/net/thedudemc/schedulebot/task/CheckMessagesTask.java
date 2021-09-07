package net.thedudemc.schedulebot.task;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.database.DatabaseManager;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.models.ScheduledMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckMessagesTask extends SchedulerTask {

    public CheckMessagesTask(int interval, TimeUnit units) {
        super(interval, units);
    }

    @Override
    public void run() {
        List<Integer> executable = DatabaseManager.getInstance().getMessageDao().shouldExecute();
        if (executable.isEmpty()) return;

        List<ScheduledMessage> messages = DatabaseManager.getInstance().getMessageDao().getMessagesToExecute(executable);

        messages.forEach(scheduledMessage -> {
            TextChannel channel = ScheduleBot.getJDA().getTextChannelById(scheduledMessage.getChannelId());
            if (channel != null) {
                scheduledMessage.sendToChannel(channel);
                if (scheduledMessage.isRecurring()) {
                    scheduledMessage.setExecutionDate(getNewDate(scheduledMessage));
                    DatabaseManager.getInstance().getMessageDao().update(scheduledMessage);
                } else {
                    DatabaseManager.getInstance().getMessageDao().delete(scheduledMessage.getId());
                }
            }
        });

    }

    private LocalDateTime getNewDate(ScheduledMessage scheduledMessage) {
        LocalDateTime now = LocalDateTime.now(BotConfigs.CONFIG.getTimeZone());
        ScheduledMessage.Recurrence recurrence = scheduledMessage.getRecurrence();
        assert recurrence != null;
        int interval = recurrence.getInterval();
        TimeUnit timeUnit = recurrence.getUnit();
        ChronoUnit chronoUnit = ChronoUnit.valueOf(timeUnit.name());

        return now.plus(interval, chronoUnit);
    }
}
