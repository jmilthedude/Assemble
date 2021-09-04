package net.thedudemc.schedulebot;

import com.google.gson.annotations.Expose;

public class BotConfig extends Config {

    @Expose
    public String someString;
    @Expose
    private int someInt;

    @Override
    public String getName() {
        return "Bot";
    }

    @Override
    protected void reset() {
        someString = "Justin";
        someInt = 666;
    }
}
