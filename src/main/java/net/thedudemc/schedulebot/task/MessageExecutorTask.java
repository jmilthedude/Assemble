package net.thedudemc.schedulebot.task;

import net.dv8tion.jda.api.entities.TextChannel;
import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.database.DatabaseManager;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.models.ScheduledMessage;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageExecutorTask extends SchedulerTask {

    public MessageExecutorTask(int interval, TimeUnit units) {
        super(interval, units);
    }

    @Override
    public void run() {
        List<Integer> executableIds = DatabaseManager.getInstance().getMessageDao().getIdsReadyToExecute();
        if (executableIds.isEmpty()) return;

        List<ScheduledMessage> messages = DatabaseManager.getInstance().getMessageDao().getMessagesToExecute(executableIds);

        for (ScheduledMessage message : messages) {
            TextChannel channel = ScheduleBot.getJDA().getTextChannelById(message.getChannelId());
            if (channel == null) continue;

            message.sendToChannel(channel);

            if (message.isRecurring()) {
                message.setExecutionDate(getNewDate(message));
                DatabaseManager.getInstance().getMessageDao().update(message);
            } else {
                DatabaseManager.getInstance().getMessageDao().delete(message.getId());
            }
        }

    }

    private ZonedDateTime getNewDate(ScheduledMessage scheduledMessage) {
        ZonedDateTime now = ZonedDateTime.now(BotConfigs.CONFIG.getTimeZone());
        ScheduledMessage.Recurrence recurrence = scheduledMessage.getRecurrence();
        assert recurrence != null;
        int interval = recurrence.getInterval();
        ChronoUnit chronoUnit = recurrence.getUnit();

        return now.plus(interval, chronoUnit).truncatedTo(ChronoUnit.MINUTES);
    }
}
