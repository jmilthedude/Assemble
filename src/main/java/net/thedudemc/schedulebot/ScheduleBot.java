package net.thedudemc.schedulebot;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleBot {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleBot.class);
    public static JDA JDA;

    public static void main(String[] args) {
        getLogger().info("Starting up...");

//        if (args.length < 1) {
//            getLogger().error("A token must be provided as the first Program Argument.");
//            return;
//        }
//
//        String token = args[0];
//        try {
//            JDABuilder builder = JDABuilder.createDefault(token);
//            builder.enableIntents(EnumSet.allOf(GatewayIntent.class));
//            //builder.addEventListeners(new BotSetup());
//            JDA = builder.build();
//        } catch (LoginException ex) {
//            getLogger().error("Invalid Token...");
//        }
    }

    public static Logger getLogger() {
        return LOG;
    }

    public static JDA getJDA() {
        return JDA;
    }
}
