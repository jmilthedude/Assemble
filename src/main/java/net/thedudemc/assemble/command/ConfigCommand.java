package net.thedudemc.assemble.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.thedudemc.assemble.init.BotConfigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.ZoneId;
import java.util.List;

public class ConfigCommand implements ICommand {

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String getDescription() {
        return "Set various config options.";
    }

    @Override
    public void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel channel, Message message, @Nullable String[] args) {
        if (args == null || args.length == 0) {
            replyError((TextChannel) channel, "Too few arguments.");
            return;
        }

        if (args.length == 1) {
            if ("help".equalsIgnoreCase(args[0])) {
                printHelpMessage((TextChannel) channel);
            } else {
                replyError((TextChannel) channel, "Invalid Arguments.");
            }
        } else if (args.length == 2) { //config timezone <timezone> || config roles list
            if (args[0] == null || args[1] == null) {
                replyError((TextChannel) channel, "Invalid Arguments.");
                return;
            }
            if ("timezone".equalsIgnoreCase(args[0])) {
                try {
                    ZoneId.of(args[1]);
                    if (BotConfigs.CONFIG.setTimeZone(args[1])) {
                        replySuccess((TextChannel) channel, "The Time Zone was successfully changed: " + args[1]);
                    }
                } catch (Exception ex) {
                    replyError((TextChannel) channel, "Invalid Time Zone ID: " + args[1]);
                }
            } else if ("roles".equalsIgnoreCase(args[0])) {
                if ("list".equalsIgnoreCase(args[1])) {
                    printRoles((TextChannel) channel);
                } else {
                    replyError((TextChannel) channel, "Invalid Arguments.");
                }
            } else {
                replyError((TextChannel) channel, "That is not a valid config option.");
            }
        } else if (args.length == 3) { // config roles <add/remove> <role>
            if (args[0] == null || args[1] == null || args[2] == null) {
                replyError((TextChannel) channel, "Invalid Arguments.");
                return;
            }
            String role = args[2];
            if ("roles".equalsIgnoreCase(args[0])) {
                if ("add".equalsIgnoreCase(args[1])) {
                    if (BotConfigs.CONFIG.addPermittedRole(role)) {
                        replySuccess((TextChannel) channel, "The role was successfully added: " + args[2]);
                    } else {
                        replyError((TextChannel) channel, "That role already exists!");
                    }

                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (BotConfigs.CONFIG.removeRole(role)) {
                        replySuccess((TextChannel) channel, "The role was successfully removed: " + args[2]);
                    } else {
                        replyError((TextChannel) channel, "That role does not exist!");
                    }
                } else {
                    replyError((TextChannel) channel, "Invalid Arguments.");
                }
            } else {
                replyError((TextChannel) channel, "That is not a valid config option.");
            }
        } else {
            replyError((TextChannel) channel, "Invalid number of arguments.");
        }
    }

    private void printHelpMessage(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Config Help")
                .setColor(Color.ORANGE)
                .addField("List Roles", "\"-config roles list\"", false)
                .addField("Add Role", "\"-config roles add <roleName>\"", false)
                .addField("Remove Role", "\"-config roles remove <roleName>\"", false)
                .addField("Set Timezone", "\"-config timezone <timeZoneId>\"", false);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    private void printRoles(TextChannel channel) {
        List<String> roles = BotConfigs.CONFIG.getPermittedRoles();
        StringBuilder response = new StringBuilder();
        for (String role : roles) {
            response.append(role).append("\n");
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Permitted Roles")
                .addField("", response.toString(), false)
                .setColor(Color.CYAN);

        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public boolean canExecute(Member member) {
        return member.isOwner() || BotConfigs.CONFIG.hasPermission(member);
    }
}
