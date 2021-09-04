package net.thedudemc.schedulebot.init;

import net.thedudemc.schedulebot.config.BotConfig;

public class BotConfigs {

    public static BotConfig CONFIG;

    public static void register() {
        CONFIG = (BotConfig) new BotConfig().readConfig();
    }
}
