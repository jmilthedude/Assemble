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
        this.permittedRoles.add("Admin");
        this.permittedRoles.add("Moderator");
    }

    public boolean hasPermission(Member member) {
        return member.getRoles().stream().anyMatch(this::hasPermission);
    }

    private boolean hasPermission(Role role) {
        return this.hasPermission(role.getName());
    }

    private boolean hasPermission(String role) {
        return this.permittedRoles.contains(role);
    }

    public void addPermittedRole(String role) {
        if (!this.permittedRoles.contains(role)) this.permittedRoles.add(role);
    }

    public void removeRole(String role) {
        this.permittedRoles.remove(role);
    }
}
