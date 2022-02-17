package net.ninjadev.assemble.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.ninjadev.assemble.models.ScheduledMessage;
import net.ninjadev.assemble.util.EditOption;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;

public class EditListener extends ListenerAdapter {

    private static final HashMap<Long, ScheduledMessage> editingUsers = new HashMap<>();
    private static final HashMap<Long, EditState> userStates = new HashMap<>();

    public static void addEditingUser(long id, ScheduledMessage message) {
        editingUsers.put(id, message);
        setUserState(id, EditState.PRE);
    }

    public static void removeEditingUser(long id) {
        editingUsers.remove(id);
        removeUserState(id);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        Member member = event.getMember();
        if (!editingUsers.containsKey(member.getIdLong())) {
            return;
        }
        ScheduledMessage message = editingUsers.get(member.getIdLong());

        String unicode = EncodingUtil.decodeCodepoint(event.getReactionEmote().getAsCodepoints());
        EditOption option = EditOption.getFromUnicode(unicode);

        switch (option) {
            case TITLE -> message.setState(ScheduledMessage.SetupState.TITLE);
            case CONTENT -> message.setState(ScheduledMessage.SetupState.CONTENT);
            case CHANNEL -> message.setState(ScheduledMessage.SetupState.CHANNEL);
            case DATE -> message.setState(ScheduledMessage.SetupState.DATE);
            case INTERVAL -> message.setState(ScheduledMessage.SetupState.RECURRING);
            case IMAGE -> message.setState(ScheduledMessage.SetupState.IMAGE);
            default -> {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Canceled!")
                        .addField("", "You have cancelled the edit.", false)
                        .setColor(Color.GREEN);
                event.getChannel().sendMessageEmbeds(builder.build()).queue();
                removeEditingUser(member.getIdLong());
                return;
            }
        }
        message.setEditMode(true);
        SetupListener.initiateScheduleEdit(member, message, event.getChannel());
        removeEditingUser(member.getIdLong());
    }

    private static void setUserState(long user, EditState state) {
        userStates.put(user, state);
    }

    private static void removeUserState(long user) {
        userStates.remove(user);
    }

    enum EditState {
        PRE,
        TITLE,
        CONTENT,
        CHANNEL,
        DATE,
        INTERVAL,
        IMAGE,
        CONFIRM,
        DONE
    }
}
