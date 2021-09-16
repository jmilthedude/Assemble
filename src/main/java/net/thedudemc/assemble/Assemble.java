package net.thedudemc.assemble;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.thedudemc.assemble.init.BotCommands;
import net.thedudemc.assemble.init.BotConfigs;
import net.thedudemc.assemble.init.BotListeners;
import net.thedudemc.assemble.init.BotTasks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Assemble extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Assemble.class);
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
            builder.addEventListeners(new Assemble());
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
        BotTasks.register();

        Assemble.getLogger().info("Assemble setup complete!");
    }

    public static Logger getLogger() {
        return LOG;
    }

    public static JDA getJDA() {
        return JDA;
    }
}
