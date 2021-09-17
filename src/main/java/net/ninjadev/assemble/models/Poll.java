package net.ninjadev.assemble.models;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.ninjadev.assemble.util.StringUtil;

import java.util.Arrays;

public class Poll {

    String title;
    String[] answers;


    public Poll(String title, String[] answers) {
        this.title = title;
        this.answers = answers;
    }

    public String getTitle() {
        return title;
    }

    public String[] getAnswers() {
        return answers;
    }

    public static Poll parse(String input) throws IndexOutOfBoundsException {
        String title = input.substring(input.indexOf("{") + 1, input.indexOf("}"));
        String[] options = input.substring(input.indexOf("[") + 1, input.indexOf("]")).split(",");
        String[] trimmed = Arrays.stream(options).map(String::trim).toArray(String[]::new);

        return new Poll(title, trimmed);
    }

    public MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getTitle());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getAnswers().length; i++) {
            String option = this.answers[i];
            sb.append(StringUtil.getEmojiString(i + 1)).append(" ").append(option).append("\n");
        }
        builder.addField("Options:", sb.toString(), false);
        return builder.build();
    }
}
