package net.ninjadev.assemble.init;

import net.ninjadev.assemble.config.BotConfig;

public class BotConfigs {

    public static BotConfig CONFIG;

    public static void register() {
        CONFIG = (BotConfig) new BotConfig().readConfig();
    }
}
