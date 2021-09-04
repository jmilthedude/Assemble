package net.thedudemc.schedulebot.init;

import net.thedudemc.schedulebot.command.ICommand;
import net.thedudemc.schedulebot.command.ScheduleCommand;

import java.util.HashMap;

public class BotCommands {

    private static final HashMap<String, ICommand> COMMANDS = new HashMap<>();

    public static void register() {
        register(new ScheduleCommand());
    }

    private static void register(ICommand command) {
        COMMANDS.put(command.getName(), command);
    }

    public static ICommand getCommand(String commandName) {
        if (!COMMANDS.containsKey(commandName))
            throw new IllegalArgumentException("There is no command with the name: " + commandName);

        return COMMANDS.get(commandName);
    }
}