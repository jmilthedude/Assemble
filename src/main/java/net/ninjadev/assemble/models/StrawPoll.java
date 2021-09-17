package net.ninjadev.assemble.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.init.BotConfigs;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StrawPoll {
    Poll poll;

    public StrawPoll(Poll poll) {
        this.poll = poll;
    }

    public Poll getPoll() {
        return poll;
    }

    public static HttpClient getClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public HttpRequest getPostRequest(String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://strawpoll.com/api/poll"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("API-KEY", BotConfigs.CONFIG.getStrawpollKey())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    public static HttpRequest getDeleteRequest(String id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://strawpoll.com/api/content/delete"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("API-KEY", BotConfigs.CONFIG.getStrawpollKey())
                .method("DELETE", HttpRequest.BodyPublishers.ofString("{ \"content_id\": \"" + id + "\" }"))
                .build();
    }

    public static void sendPollRequest(MessageChannel channel, HttpRequest request, HttpClient client, boolean delete) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> handleResponse(channel, response, delete));
    }


    private static void handleResponse(MessageChannel channel, String response, boolean delete) {
        JsonObject json = new Gson().fromJson(response, JsonObject.class);
        if (json.get("success").getAsInt() != 1) {
            Assemble.getLogger().error(json.get("message").getAsString());
            return;
        }
        if (delete) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Success!")
                            .addField("", "You have deleted the strawpoll!", false)
                            .setColor(Color.GREEN).build())
                    .queue();
            return;
        }
        String contentId = json.get("content_id").getAsString();
        String url = "https://strawpoll.com/" + contentId;
        channel.sendMessage(new MessageBuilder(url).build()).queue();
    }
}
