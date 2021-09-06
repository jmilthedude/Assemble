package net.thedudemc.schedulebot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.thedudemc.schedulebot.init.BotCommands;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.init.BotListeners;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class ScheduleBot extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleBot.class);
    public static JDA JDA;

    public static void main(String[] args) {
        getLogger().info("Starting up...");

        if (args.length < 1) {
            getLogger().error("A token must be provided as the first Program Argument.");
            return;
        }

        String token = args[0];
        try {
            JDABuilder builder = JDABuilder.createDefault(token);
            builder.enableIntents(EnumSet.allOf(GatewayIntent.class));
            builder.addEventListeners(new ScheduleBot());
            JDA = builder.build();
        } catch (LoginException ex) {
            getLogger().error("Invalid Token...");
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        BotConfigs.register();
        BotListeners.register();
        BotCommands.register();

        ScheduleBot.getLogger().info("ScheduleBot setup complete!");
    }

    public static Logger getLogger() {
        return LOG;
    }

    public static JDA getJDA() {
        return JDA;
    }
}
