package net.ninjadev.assemble.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.*;
import net.ninjadev.assemble.models.Poll;
import net.ninjadev.assemble.models.StrawPoll;
import net.ninjadev.assemble.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class PollCommand implements ICommand {


    static final Gson gson = new GsonBuilder().create();

    @Override
    public String getName() {
        return "poll";
    }

    @Override
    public String getDescription() {
        return "Create a poll, discord or strawpoll";
    }

    //-poll [s/d] {title} [option 1, option 2, option 3]
    @Override
    public void execute(@NotNull Guild guild, @NotNull Member member, MessageChannel channel, Message message, String[] args) {
        if (args == null || args.length == 0) return; // this command requires arguments
        if (!message.isFromType(ChannelType.TEXT)) return;

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                String contentId = args[1];
                deleteStrawPoll((TextChannel) channel, contentId);
                message.delete().queue();
            } else {
                replyError((TextChannel) channel, "Invalid Arguments...");
            }
        } else {

            String type = args[0];

            String input = message.getContentRaw().substring(message.getContentRaw().indexOf(type) + 1);
            try {
                Poll poll = Poll.parse(input);
                if (type.equalsIgnoreCase("d")) {
                    createDiscordPoll(channel, poll);
                } else if (type.equalsIgnoreCase("s")) {
                    createStrawPoll(channel, poll);
                } else {
                    replyError((TextChannel) channel, "The type of poll must be \"s\" or \"d\".");
                }
            } catch (IndexOutOfBoundsException exception) {
                replyError((TextChannel) channel, "There was an error while trying to parse the poll command.");
            } finally {
                message.delete().queue();
            }
        }

    }

    private void deleteStrawPoll(TextChannel channel, String contentId) {
        HttpClient client = StrawPoll.getClient();
        HttpRequest request = StrawPoll.getDeleteRequest(contentId);
        StrawPoll.sendPollRequest(channel, request, client, true);
    }

    private void createStrawPoll(MessageChannel channel, Poll poll) {
        StrawPoll strawPoll = new StrawPoll(poll);
        String json = gson.toJson(strawPoll);
        HttpClient client = StrawPoll.getClient();
        HttpRequest request = strawPoll.getPostRequest(json);

        StrawPoll.sendPollRequest(channel, request, client, false);
    }

    private void createDiscordPoll(MessageChannel channel, Poll poll) {
        channel.sendMessageEmbeds(poll.getEmbed()).queue(message -> addReactions(message, poll));
    }

    private void addReactions(Message message, Poll poll) {
        for (int i = 0; i < poll.getAnswers().length; i++) {
            Emoji emoji = EmojiManager.getForAlias(StringUtil.getEmojiString(i + 1));
            message.addReaction(emoji.getUnicode()).queue();
        }
    }

}
