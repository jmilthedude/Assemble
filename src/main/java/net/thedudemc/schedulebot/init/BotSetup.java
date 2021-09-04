package net.thedudemc.schedulebot.init;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thedudemc.schedulebot.ScheduleBot;
import org.jetbrains.annotations.NotNull;

public class BotSetup extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        BotConfigs.register();
        //BotData.register();
        BotListeners.register();
        BotCommands.register();

        ScheduleBot.getLogger().info("ScheduleBot setup complete!");
    }
}
