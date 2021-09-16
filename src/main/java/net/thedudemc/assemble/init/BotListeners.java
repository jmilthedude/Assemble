package net.thedudemc.assemble.init;

import net.thedudemc.assemble.Assemble;
import net.thedudemc.assemble.listener.CommandListener;
import net.thedudemc.assemble.listener.SetupListener;

public class BotListeners {

    public static void register() {
        Assemble.getJDA().addEventListener(
                new CommandListener(),
                new SetupListener()
        );
    }
}
