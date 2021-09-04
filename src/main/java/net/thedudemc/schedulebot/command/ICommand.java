package net.thedudemc.schedulebot.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ICommand {

    String getName();

    String getDescription();

    void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel channel, Message message, @Nullable String[] args);

    boolean canExecute(Member member);
}
