package net.thedudemc.schedulebot;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleBot {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleBot.class);
    public static JDA JDA;

    public static void main(String[] args) {
        getLogger().info("Starting up...");

        BotConfig config = new BotConfig().readConfig();

        System.out.println(config.someString);

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

    public static class SomeObject {
        private String name;
        private int id;

        public SomeObject(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "SomeObject{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }
    }

    public static Logger getLogger() {
        return LOG;
    }

    public static JDA getJDA() {
        return JDA;
    }
}
