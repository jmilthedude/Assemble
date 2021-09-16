package net.thedudemc.assemble.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;

public interface ICommand {

    String getName();

    String getDescription();

    void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel channel, Message message, @Nullable String[] args);

    boolean canExecute(Member member);

    default void replyError(TextChannel channel, String response) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Invalid Command")
                .addField("Error Message", response, false)
                .setFooter("Type \"-" + this.getName() + " help\" for usage.")
                .setColor(Color.RED);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    default void replySuccess(TextChannel channel, String response) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Success!")
                .addField("", response, false)
                .setColor(Color.GREEN);
        channel.sendMessageEmbeds(builder.build()).queue();
    }
}
