package net.thedudemc.schedulebot.models;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.thedudemc.schedulebot.ScheduleBot;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduledMessage {
    private int id;
    private String title;
    private String content;
    private long channelId;
    private long ownerId;
    private LocalDateTime executionDate;
    private boolean recurring;
    @Nullable
    private Recurrence recurrence;
    @Nullable
    private String imageFileName;
    private SetupState state;

    public ScheduledMessage(Long ownerId) {
        this.ownerId = ownerId;
        this.state = SetupState.NEW;
    }

    public ScheduledMessage(int id, String title, String content,
                            long channelId, long ownerId, LocalDateTime executionDate,
                            boolean recurring, @Nullable Recurrence recurrence, @Nullable String imageFileName,
                            SetupState state) {
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

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
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


    public void sendToChannel(TextChannel channel) {
        if (this.imageFileName != null && !this.imageFileName.isEmpty()) {
            File imageFile = new File("./images/" + this.imageFileName);

            channel.sendFile(imageFile)
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(this.title)
                            .addField("", this.content, true)
                            .setImage("attachment://" + this.imageFileName)
                            .build()).queue();
        } else {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(this.title)
                    .addField("", this.content, true)
                    .build()).queue();
        }
    }

    public MessageEmbed getStatusEmbed() {
        switch (this.getState()) {
            case NEW -> {
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
                        .addField("", "example: " + ScheduleBot.getJDA().getGuilds().get(0).getTextChannels().get(0).getAsMention(), false)
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
                        .addField("", "allowed units: x seconds, x minutes, x hours, x days", false)
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
}
