package net.thedudemc.assemble.init;

import net.thedudemc.assemble.config.BotConfig;

public class BotConfigs {

    public static BotConfig CONFIG;

    public static void register() {
        CONFIG = (BotConfig) new BotConfig().readConfig();
    }
}
