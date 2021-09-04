package net.thedudemc.schedulebot.init;

import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.listener.CommandListener;

public class BotListeners {

    public static void register() {
        ScheduleBot.getJDA().addEventListener(
                new CommandListener()
        );
    }
}
