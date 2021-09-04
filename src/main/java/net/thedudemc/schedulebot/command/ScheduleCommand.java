package net.thedudemc.schedulebot.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.listener.SetupListener;
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
        if (args == null || args.length == 0) return; // this command requires arguments
        if (!channel.getName().equalsIgnoreCase("schedule-setup")) return;

        if (args.length == 1) {
            if ("new".equalsIgnoreCase(args[0])) {
                if (!SetupListener.initiateScheduleSetup(member)) {
                    // active setup already exists.
                }
            } else if ("help".equalsIgnoreCase(args[0])) {

            }
        } else if (args.length == 2) {
            String messageIdInput = args[1];
            int messageId = Integer.parseInt(messageIdInput);
            if ("edit".equalsIgnoreCase(args[0])) {

            } else if ("delete".equalsIgnoreCase(args[0])) {

            } else {

            }
        }
        ScheduleBot.getLogger().info(this.getName() + ": " + this.getDescription());
    }

    @Override
    public boolean canExecute(Member member) {
        return member.isOwner() || BotConfigs.CONFIG.hasPermission(member);
    }
}
