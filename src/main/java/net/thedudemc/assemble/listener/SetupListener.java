package net.thedudemc.assemble.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thedudemc.assemble.Assemble;
import net.thedudemc.assemble.database.DatabaseManager;
import net.thedudemc.assemble.init.BotConfigs;
import net.thedudemc.assemble.models.ScheduledMessage;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
            replySuccess(event.getChannel(), "You have cancelled this scheduled message setup.");
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
        scheduledMessage.setState(ScheduledMessage.SetupState.TITLE);
    }

    private void setMessageTitle(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String title = message.getContentRaw();
        scheduledMessage.setTitle(title);
        scheduledMessage.setState(ScheduledMessage.SetupState.CONTENT);

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageContent(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String content = message.getContentRaw();
        scheduledMessage.setContent(content);
        scheduledMessage.setState(ScheduledMessage.SetupState.CHANNEL);

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageChannel(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        Optional<TextChannel> requestedOptional = message.getMentionedChannels().stream().findAny();
        if (requestedOptional.isPresent()) {
            TextChannel requestedChannel = requestedOptional.get();
            scheduledMessage.setChannelId(requestedChannel.getIdLong());
            scheduledMessage.setState(ScheduledMessage.SetupState.DATE);

            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
        } else {
            replyError(channel, "No channel found by that name.");
        }
    }

    private void setMessageDate(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("MM/dd/yyyy HH:mm")
                    .toFormatter();
            ZonedDateTime date = LocalDateTime.parse(message.getContentRaw(), formatter).atZone(BotConfigs.CONFIG.getTimeZone());
            ZonedDateTime now = ZonedDateTime.now(BotConfigs.CONFIG.getTimeZone());
            if (date.isBefore(now)) {
                replyError(channel, "Cannot set to a past date/time");
                return;
            }
            scheduledMessage.setExecutionDate(date);
            scheduledMessage.setState(ScheduledMessage.SetupState.RECURRING);

            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
        } catch (IllegalArgumentException exception) {
            Assemble.getLogger().error(exception.getMessage());
        } catch (DateTimeException exception) {
            replyError(channel, "Invalid date. Try again with format \"MM/dd/yyyy HH:mm\"");
        }
    }

    private void setMessageRecurrence(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        String content = message.getContentRaw();
        if (content.equalsIgnoreCase("none")) {
            scheduledMessage.setRecurring(false);
            scheduledMessage.setRecurrence(null);
            scheduledMessage.setState(ScheduledMessage.SetupState.IMAGE);
        } else {
            try {
                String[] args = content.split(" ");
                if (args.length == 2) {
                    int interval = Integer.parseInt(args[0]);
                    ChronoUnit unit = ChronoUnit.valueOf(args[1].toUpperCase());
                    ScheduledMessage.Recurrence recurrence = new ScheduledMessage.Recurrence(interval, unit);
                    scheduledMessage.setRecurring(true);
                    scheduledMessage.setRecurrence(recurrence);
                    scheduledMessage.setState(ScheduledMessage.SetupState.IMAGE);

                } else
                    throw new IllegalArgumentException("Invalid arguments. Must be <number> <timeUnit> (ie: 20 minutes, 24 hours, 7 days, 1 years)");
            } catch (NumberFormatException exception) {
                replyError(channel, "Invalid arguments. First argument must be a number.");
            } catch (IllegalArgumentException exception) {
                replyError(channel, "Invalid argument. Second argument must be an appropriate unit of time.");
            }
        }

        channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
    }

    private void setMessageImage(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        try {
            if (message.getContentRaw().equalsIgnoreCase("none")) {
                scheduledMessage.setImageFileName("");
                scheduledMessage.setState(ScheduledMessage.SetupState.CONFIRM);

                scheduledMessage.sendToChannel(channel);
                channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
            } else if (message.getAttachments().isEmpty()) throw new IllegalArgumentException("No image uploaded.");

            List<Message.Attachment> attachments = message.getAttachments();
            Message.Attachment image = attachments.stream().findFirst().orElse(null);
            if (image != null && image.isImage()) {
                File path = new File("./images/");
                path.mkdirs();
                image.downloadToFile("./images/" + image.getFileName())
                        .thenAccept(file -> {
                            Assemble.getLogger().info("Saved attachment to " + file.getName());

                            scheduledMessage.setImageFileName(image.getFileName());
                            scheduledMessage.setState(ScheduledMessage.SetupState.CONFIRM);

                            scheduledMessage.sendToChannel(channel);

                            channel.sendMessageEmbeds(scheduledMessage.getStatusEmbed()).queue();
                        })
                        .exceptionally(exception ->
                        {
                            Assemble.getLogger().error(exception.getMessage());
                            replyError(channel, "There was a problem downloading the image.");
                            return null;
                        });
            }
        } catch (IllegalArgumentException exception) {
            replyError(channel, exception.getMessage());
        }
    }

    private void confirmMessage(ScheduledMessage scheduledMessage, TextChannel channel, Message message) {
        if (message.getContentRaw().equalsIgnoreCase("confirm")) {
            scheduledMessage.setState(ScheduledMessage.SetupState.READY);

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

    private void replyError(TextChannel channel, String response) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Invalid Command")
                .addField("Error Message", response, false)
                .setColor(Color.RED);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    private void replySuccess(TextChannel channel, String response) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Success!")
                .addField("", response, false)
                .setColor(Color.GREEN);
        channel.sendMessageEmbeds(builder.build()).queue();
    }
}
