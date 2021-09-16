package net.ninjadev.assemble.init;

import net.ninjadev.assemble.command.ConfigCommand;
import net.ninjadev.assemble.command.ICommand;
import net.ninjadev.assemble.command.ScheduleCommand;

import java.util.HashMap;

public class BotCommands {

    private static final HashMap<String, ICommand> COMMANDS = new HashMap<>();

    public static void register() {
        register(new ScheduleCommand());
        register(new ConfigCommand());
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
