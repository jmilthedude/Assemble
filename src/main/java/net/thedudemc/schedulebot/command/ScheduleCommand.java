package net.thedudemc.schedulebot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.thedudemc.schedulebot.database.DatabaseManager;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.listener.SetupListener;
import net.thedudemc.schedulebot.models.ScheduledMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleCommand implements ICommand {
    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public String getDescription() {
        return "Root command for scheduling messages.";
    }

    @Override
    public void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel messageChannel, Message message, @Nullable String[] args) {
        if (args == null || args.length == 0) return; // this command requires arguments
        if (!message.isFromType(ChannelType.TEXT)) return;
        if (!messageChannel.getName().equalsIgnoreCase("schedule-setup")) {
            replyError((TextChannel) messageChannel, "You cannot send that command in this channel.");
            return;
        }

        TextChannel channel = (TextChannel) messageChannel;


        if (args.length == 1) {
            if ("new".equalsIgnoreCase(args[0])) {
                if (!SetupListener.initiateScheduleSetup(member)) {
                    replyError(channel, "You cannot start a new scheduled message while actively creating one.");
                }
            } else if ("list".equalsIgnoreCase(args[0])) {
                List<ScheduledMessage> messages = DatabaseManager.getInstance().getMessageDao().selectAll();
                if (!messages.isEmpty()) {
                    sendMessageList(channel, messages);
                } else {
                    replyError(channel, "There are no scheduled messages to list.");
                }
            } else if ("help".equalsIgnoreCase(args[0])) {
                printHelpMessage(channel);
            } else {
                replyError(channel, "Invalid Argument.");
            }
        } else if (args.length == 2) {
            String messageIdInput = args[1];
            if (messageIdInput == null) throw new IllegalArgumentException();
            int messageId = Integer.parseInt(messageIdInput);
            if ("show".equalsIgnoreCase(args[0])) {
                ScheduledMessage scheduledMessage = DatabaseManager.getInstance().getMessageDao().select(messageId);
                if (scheduledMessage != null) {
                    scheduledMessage.sendToChannel(channel);
                } else {
                    replyError(channel, "No message found with that ID.");
                }
            } else if ("delete".equalsIgnoreCase(args[0])) {
                if (DatabaseManager.getInstance().getMessageDao().delete(messageId)) {
                    replySuccess(channel, "The message was successfully deleted. ID: " + messageId);
                } else {
                    replyError(channel, "No message found with that ID or there was an error. See console if you believe this was an error.");
                }
            } else {
                replyError(channel, "Invalid Argument.");
            }
        } else {
            replyError(channel, "Invalid Arguments.");
        }
    }

    @Override
    public boolean canExecute(Member member) {
        return member.isOwner() || BotConfigs.CONFIG.hasPermission(member);
    }

    private void sendMessageList(TextChannel channel, List<ScheduledMessage> messages) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Scheduled Messages")
                .setColor(Color.CYAN);
        messages.forEach(message -> {
            String messageBody = "Title: " + message.getTitle() + "\n" +
                    "Execution Date: " + message.getExecutionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) + "\n";
            if (message.isRecurring() && message.getRecurrence() != null) {
                messageBody += "Interval: Every " + message.getRecurrence().getInterval() + " " + message.getRecurrence().getUnit().toString().toLowerCase() + "\n";
            }
            builder.addField("Message ID: " + message.getId(), messageBody, false);
        });

        channel.sendMessageEmbeds(builder.build()).queue();
    }

    private void printHelpMessage(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Schedule Help")
                .setColor(Color.ORANGE)
                .addField("Instructions", "Simply start a new Scheduled Message setup and follow the prompts.", false)
                .addBlankField(false)
                .addField("Start Setup", "\"-schedule new\"", false)
                .addField("List Scheduled Messages", "\"-schedule list\"", false)
                .addField("Delete Message", "\"-schedule delete <ID>\"", false);
        channel.sendMessageEmbeds(builder.build()).queue();
    }
}
