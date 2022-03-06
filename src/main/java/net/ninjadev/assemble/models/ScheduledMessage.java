package net.ninjadev.assemble.models;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.init.BotConfigs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;

public class ScheduledMessage {
    private int id;
    private String title;
    private String content;
    private long channelId;
    private final long ownerId;
    private ZonedDateTime executionDate;
    private boolean recurring;
    @Nullable
    private Recurrence recurrence;
    @Nullable
    private String imageFileName;
    private SetupState state;
    private boolean editMode;
    private boolean lastDay;

    public ScheduledMessage(Long ownerId) {
        this.ownerId = ownerId;
        this.state = SetupState.NEW;
    }

    public ScheduledMessage(int id, String title, String content,
                            long channelId, long ownerId, ZonedDateTime executionDate,
                            boolean recurring, @Nullable Recurrence recurrence, @Nullable String imageFileName,
                            SetupState state, boolean isLastDay) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.executionDate = executionDate;
        this.recurring = recurring;
        this.recurrence = recurrence;
        this.imageFileName = imageFileName;
        this.state = state;
        this.editMode = false;
        this.lastDay = isLastDay;
    }

    public static ScheduledMessage ofDaily(long ownerId, long channelId, ZonedDateTime executionDate, String message) {
        ScheduledMessage scheduledMessage = new ScheduledMessage(ownerId);
        scheduledMessage.setChannelId(channelId);
        scheduledMessage.setExecutionDate(executionDate);
        scheduledMessage.setTitle(message);
        scheduledMessage.setRecurring(true);
        scheduledMessage.setRecurrence(new Recurrence(1, ChronoUnit.DAYS));
        scheduledMessage.setContent("");
        scheduledMessage.setState(SetupState.READY);
        return scheduledMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public ZonedDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(ZonedDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public @Nullable
    Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(@Nullable Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public @Nullable
    String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(@Nullable String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public SetupState getState() {
        return state;
    }

    public void setState(SetupState state) {
        this.state = state;
    }


    public void send(TextChannel channel, boolean test) {
        EmbedBuilder builder = new EmbedBuilder();
        final boolean hasMentions = this.hasMentions();
        String title = this.title;
        if (this.title.equalsIgnoreCase("Daily")) {
            int day = ZonedDateTime.now(BotConfigs.CONFIG.getTimeZone()).getDayOfMonth();
            title = "Day " + day;
        }
        builder.setTitle(title);
        builder.setColor(Color.MAGENTA);
        if (this.content != null && !this.content.isEmpty()) {
            if (this.content.length() > 1024) {
                String[] parts = splitPreservingWords(this.content, 1024);
                Arrays.stream(parts).forEach(part -> builder.addField("", part, false));
            } else {
                builder.addField("", this.content, false);
            }
        }
        if (this.imageFileName != null && !this.imageFileName.isEmpty()) {
            File imageFile = new File("./images/" + this.imageFileName);
            builder.setImage("attachment://" + this.imageFileName);
            channel.sendFile(imageFile).setEmbeds(builder.build()).queue(message -> {
                if (this.title.equalsIgnoreCase("daily")) {
                    message.addReaction("\u2705").queue();
                }
                if (hasMentions) {
                    sendMentions(channel);
                }
            });
        } else {
            channel.sendMessageEmbeds(builder.build()).queue(message -> {
                if (this.title.equalsIgnoreCase("daily")) {
                    message.addReaction("\u2705").queue();
                    message.addReaction("\u274C").queue();
                }
                if (hasMentions) {
                    sendMentions(channel);
                }
            });
        }
        if (test) {
            EmbedBuilder embedBuilder = getMessageDetails();
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    private String[] splitPreservingWords(String text, int length) {
        return text.replaceAll("(?:\\s*)(.{1," + length + "})(?:\\s+|\\s*$)", "$1\n").split("\n");
    }

    @NotNull
    private EmbedBuilder getMessageDetails() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Message Details")
                .setColor(Color.CYAN);
        String messageBody = "Execution Date: " + this.getExecutionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) + "\n";
        if (this.isRecurring() && this.getRecurrence() != null) {
            messageBody += "Interval: Every " + this.getRecurrence().getInterval() + " " + this.getRecurrence().getUnit().toString().toLowerCase() + "\n";
        }
        GuildChannel assigned = Assemble.getJDA().getGuildChannelById(this.getChannelId());
        if (assigned != null) {
            Category category = assigned.getParent();
            if (category != null) {
                messageBody += assigned.getAsMention() + " in " + category.getAsMention() + "\n";
            }
        }
        messageBody += "\n\n";
        embedBuilder.addField("Message ID: " + this.getId(), messageBody, false);
        return embedBuilder;
    }

    private void sendMentions(TextChannel channel) {
        List<String> mentions = this.getMentions();
        MessageBuilder messageBuilder = new MessageBuilder();
        mentions.forEach(s -> messageBuilder.append(s).append(" "));
        channel.sendMessage(messageBuilder.build()).queue();
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean mode) {
        this.editMode = mode;
    }

    public void setLastDay(boolean lastDay) {
        this.lastDay = lastDay;
    }

    public boolean isLastDay() {
        return lastDay;
    }

    public MessageEmbed getStatusEmbed() {
        switch (this.getState()) {
            case NEW, TITLE -> {
                return new EmbedBuilder()
                        .setTitle("Set a Title")
                        .addField("", "example: Scheduled Message", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case CONTENT -> {
                return new EmbedBuilder()
                        .setTitle("Set Content")
                        .addField("", "Type whatever you want your message to say. You may use discord markdowns such as ~~strikethrough~~.", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case CHANNEL -> {
                return new EmbedBuilder()
                        .setTitle("Set the channel.")
                        .addField("", "example: " + Assemble.getJDA().getGuilds().get(0).getTextChannels().get(0).getAsMention(), false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case DATE -> {
                return new EmbedBuilder()
                        .setTitle("Set the date.")
                        .addField("", "Write in \"MM/dd/yyyy HH:mm\" format. ie 12/31/2021 14:30 for December 31, 2021 2:30pm", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case RECURRING -> {
                return new EmbedBuilder()
                        .setTitle("Set repeat interval.")
                        .addField("", "Type \"none\" if to schedule this message once.", false)
                        .addField("", "example: 1 minutes or 30 days.", false)
                        .addField("", "allowed units: x minutes, x hours, x days", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case IMAGE -> {
                return new EmbedBuilder()
                        .setTitle("Add an image.")
                        .addField("", "Type \"none\" if you do not wish to attach an image, or upload one.", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case CONFIRM -> {
                return new EmbedBuilder()
                        .setTitle("Confirm.")
                        .addField("", "Type \"confirm\" to complete.", false)
                        .setFooter("Type \"cancel\" to stop.")
                        .setColor(Color.CYAN)
                        .build();
            }
            case READY -> {
                return new EmbedBuilder()
                        .setTitle("Setup Complete!")
                        .addField("", "Message ID: " + this.getId(), false)
                        .addField("", "Scheduled for: " + this.getExecutionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")), false)
                        .setColor(Color.GREEN)
                        .build();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ScheduledMessage{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", channelId=" + channelId +
                ", ownerId=" + ownerId +
                ", executionDate=" + executionDate +
                ", recurring=" + recurring +
                ", recurrence=" + recurrence +
                ", imageFileName='" + imageFileName + '\'' +
                ", state=" + state +
                '}';
    }

    public boolean hasMentions() {
        if (this.content == null || this.content.isEmpty()) return false;
        String[] strings = this.content.split(" ");
        return Arrays.stream(strings).anyMatch(s -> s.startsWith("@"));
    }

    public List<String> getMentions() {
        return Arrays.stream(this.content.split(" ")).filter(s -> s.startsWith("@")).collect(Collectors.toList());
    }

    public static class Recurrence {

        private final int interval;
        private final ChronoUnit unit;

        public Recurrence(int interval, ChronoUnit unit) {
            this.interval = interval;
            this.unit = unit;
        }

        public int getInterval() {
            return interval;
        }

        public ChronoUnit getUnit() {
            return unit;
        }
    }

    public enum SetupState {
        NEW,
        TITLE,
        CONTENT,
        CHANNEL,
        DATE,
        RECURRING,
        IMAGE,
        CONFIRM,
        READY
    }
}
