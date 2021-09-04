package net.thedudemc.schedulebot.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.thedudemc.schedulebot.init.BotConfigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel channel, Message message, @Nullable String[] args) {

    }

    @Override
    public boolean canExecute(Member member) {
        return BotConfigs.CONFIG.hasPermission(member);
    }
}
