package net.thedudemc.assemble.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thedudemc.assemble.Assemble;
import net.thedudemc.assemble.command.ICommand;
import net.thedudemc.assemble.init.BotCommands;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMember() == null) return;
        Guild guild = event.getGuild();
        Member member = event.getMember();

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith("-")) return;

        String commandName = content.contains(" ") ? content.substring(1, content.indexOf(" ")) : content.substring(1);
        try {
            ICommand command = BotCommands.getCommand(commandName);
            String[] args = content.contains(" ") ? content.substring(content.indexOf(" ") + 1).split(" ") : null;

            if (command.canExecute(member)) {
                command.execute(guild, member, event.getChannel(), event.getMessage(), args);
            } else {
                Assemble.getLogger().error(event.getMember().getEffectiveName() + " tried to run the command '" + commandName + "' without the required permission.");
            }
        } catch (IllegalArgumentException e) {
            Assemble.getLogger().error(e.getMessage());
        }
    }
}
