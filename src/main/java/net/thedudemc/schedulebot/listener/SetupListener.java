package net.thedudemc.schedulebot.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.database.DatabaseManager;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.models.Recurrence;
import net.thedudemc.schedulebot.models.ScheduledMessage;
import net.thedudemc.schedulebot.models.SetupState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SetupListener extends ListenerAdapter {

    private static final HashMap<Long, ScheduledMessage> activeUsers = new HashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if (!event.getChannel().getName().equalsIgnoreCase("schedule-setup")) return;
        Member member = event.getMember();
        if (member == null) return;

        if (!activeUsers.containsKey(member.getIdLong())) return;


        if (event.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
            activeUsers.remove(member.getIdLong());
            return;
        }

        ScheduledMessage scheduledMessage = activeUsers.get(member.getIdLong());

        handleMessage(scheduledMessage, event.getChannel(), event.getMessage());
    }

    private void handleMessage(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        switch (scheduledMessage.getState()) {
            case NEW -> startDialog(scheduledMessage, channel, message);
            case TITLE -> setMessageTitle(scheduledMessage, channel, message);
            case CONTENT -> setMessageContent(scheduledMessage, channel, message);
            case CHANNEL -> setMessageChannel(scheduledMessage, channel, message);
            case DATE -> setMessageDate(scheduledMessage, channel, message);
            case RECURRING -> setMessageRecurrence(scheduledMessage, channel, message);
            case IMAGE -> setMessageImage(scheduledMessage, channel, message);
            case CONFIRM -> confirmMessage(scheduledMessage, channel, message);
        }
    }

    private void startDialog(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
        scheduledMessage.setState(SetupState.TITLE);
    }

    private void setMessageTitle(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String title = message.getContentRaw();
        scheduledMessage.setTitle(title);
        scheduledMessage.setState(SetupState.CONTENT);

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageContent(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String content = message.getContentRaw();
        scheduledMessage.setContent(content);
        scheduledMessage.setState(SetupState.CHANNEL);

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageChannel(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        Optional<TextChannel> requestedOptional = message.getMentionedChannels().stream().findAny();
        if (requestedOptional.isPresent()) {
            TextChannel requestedChannel = requestedOptional.get();
            scheduledMessage.setChannelId(requestedChannel.getIdLong());
            scheduledMessage.setState(SetupState.DATE);

            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
        } else {
            // no channel found
        }
    }

    private void setMessageDate(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("MM/dd/yyyy HH:mm")
                    .toFormatter();
            LocalDateTime date = LocalDateTime.parse(message.getContentRaw(), formatter);
            if (date.isBefore(ChronoLocalDateTime.from(LocalDateTime.now().atZone(BotConfigs.CONFIG.getTimeZone())))) {
                throw new IllegalArgumentException("Cannot set to a past date/time");
            }
            scheduledMessage.setExecutionDate(date);
            scheduledMessage.setState(SetupState.RECURRING);

            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();

        } catch (DateTimeParseException ex) {
            // tell user date was invalid.
        } catch (IllegalArgumentException ex) {
            // tell user it's a past date.
        } catch (DateTimeException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
    }

    private void setMessageRecurrence(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String content = message.getContentRaw();
        if (content.equalsIgnoreCase("none")) {
            scheduledMessage.setRecurring(false);
            scheduledMessage.setRecurrence(null);
            scheduledMessage.setState(SetupState.IMAGE);
        } else {
            try {
                String[] args = content.split(" ");
                if (args.length == 2) {
                    int interval = Integer.parseInt(args[0]);
                    TimeUnit unit = TimeUnit.valueOf(args[1].toUpperCase());
                    Recurrence recurrence = new Recurrence(interval, unit);
                    scheduledMessage.setRecurring(true);
                    scheduledMessage.setRecurrence(recurrence);
                    scheduledMessage.setState(SetupState.IMAGE);

                } else
                    throw new IllegalArgumentException("Invalid arguments. Must be <number> <timeUnit> (ie: 10 seconds, 20 minutes, 24 hours, 7 days)");
            } catch (NumberFormatException exception) {
                // tell user their first argument wasn't number
            } catch (IllegalArgumentException exception) {
                // tell them to use an approptiate Time Unit.
            }
        }

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageImage(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        if (message.getContentRaw().equalsIgnoreCase("none")) {
            scheduledMessage.setImageFileName("");
            scheduledMessage.setState(SetupState.CONFIRM);

            scheduledMessage.sendToChannel(channel);
            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
        } else if (message.getAttachments().isEmpty()) throw new IllegalArgumentException();

        List<Message.Attachment> attachments = message.getAttachments();
        Message.Attachment image = attachments.stream().findFirst().orElse(null);
        if (image != null && image.isImage()) {
            File path = new File("./images/");
            path.mkdirs();
            image.downloadToFile("./images/" + image.getFileName())
                    .thenAccept(file -> {
                        ScheduleBot.getLogger().info("Saved attachment to " + file.getName());

                        scheduledMessage.setImageFileName(image.getFileName());
                        scheduledMessage.setState(SetupState.CONFIRM);

                        scheduledMessage.sendToChannel(channel);

                        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
                    })
                    .exceptionally(exception ->
                    {
                        ScheduleBot.getLogger().error(exception.getMessage());
                        return null;
                    });
        }
    }

    private void confirmMessage(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        if (message.getContentRaw().equalsIgnoreCase("confirm")) {
            scheduledMessage.setState(SetupState.READY);

            int id = DatabaseManager.getInstance().getMessageDao().insert(scheduledMessage);

            scheduledMessage.setId(id);

            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();

            activeUsers.remove(scheduledMessage.getOwnerId());
        }
    }

    public static boolean initiateScheduleSetup(Member member) {
        if (activeUsers.containsKey(member.getIdLong())) return false;

        activeUsers.put(member.getIdLong(), new ScheduledMessage(member.getIdLong()));
        return true;
    }
}
