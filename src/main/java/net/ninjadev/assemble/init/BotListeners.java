package net.ninjadev.assemble.init;

import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.listener.CommandListener;
import net.ninjadev.assemble.listener.EditListener;
import net.ninjadev.assemble.listener.SetupListener;

public class BotListeners {

    public static void register() {
        Assemble.getJDA().addEventListener(
                new CommandListener(),
                new SetupListener(),
                new EditListener()
        );
    }
}
