package net.thedudemc.schedulebot.config;

import com.google.gson.annotations.Expose;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

public class BotConfig extends Config {

    @Expose
    private List<String> permittedRoles = new ArrayList<>();

    @Override
    public String getName() {
        return "Bot";
    }

    @Override
    protected void reset() {
        permittedRoles.add("Admin");
        permittedRoles.add("Moderator");
    }

    public boolean hasPermission(Member member) {
        return member.getRoles().stream().anyMatch(this::hasPermission);
    }

    private boolean hasPermission(Role role) {
        return hasPermission(role.getName());
    }

    private boolean hasPermission(String role) {
        return permittedRoles.contains(role);
    }
}
