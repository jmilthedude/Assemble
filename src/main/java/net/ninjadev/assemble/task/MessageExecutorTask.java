package net.ninjadev.assemble.task;

import net.dv8tion.jda.api.entities.TextChannel;
import net.ninjadev.assemble.models.ScheduledMessage;
import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.database.DatabaseManager;
import net.ninjadev.assemble.init.BotConfigs;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageExecutorTask extends SchedulerTask {

    public MessageExecutorTask(int interval, TimeUnit units) {
        super(interval, units);
    }

    @Override
    public void run() {
        List<Integer> executableIds = DatabaseManager.getInstance().getMessageDao().getIdsReadyToExecute();
        if (!executableIds.isEmpty()) {
            List<ScheduledMessage> messages = DatabaseManager.getInstance().getMessageDao().getMessagesToExecute(executableIds);

            for (ScheduledMessage message : messages) {
                TextChannel channel = Assemble.getJDA().getTextChannelById(message.getChannelId());
                if (channel == null) continue;

                message.send(channel, false);

                if (message.isRecurring()) {
                    message.setExecutionDate(getNewDate(message));
                    DatabaseManager.getInstance().getMessageDao().update(message);
                } else {
                    DatabaseManager.getInstance().getMessageDao().delete(message.getId());
                }
            }
        }

    }

    private ZonedDateTime getNewDate(ScheduledMessage scheduledMessage) {
        ZonedDateTime now = ZonedDateTime.now(BotConfigs.CONFIG.getTimeZone());
        ScheduledMessage.Recurrence recurrence = scheduledMessage.getRecurrence();
        assert recurrence != null;
        int interval = recurrence.getInterval();
        ChronoUnit chronoUnit = recurrence.getUnit();

        ZonedDateTime next = now.plus(interval, chronoUnit).truncatedTo(ChronoUnit.MINUTES);
        if (scheduledMessage.isLastDay()) {
            next = next.plusDays(next.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth() - next.getDayOfMonth());
        }
        return next;
    }
}
