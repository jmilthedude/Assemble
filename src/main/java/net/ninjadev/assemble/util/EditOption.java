package net.ninjadev.assemble.util;

import java.util.Arrays;

public enum EditOption {

    TITLE(":placard:", "\uD83E\uDEA7"),
    CONTENT(":speech_balloon:", "\uD83D\uDCAC"),
    CHANNEL(":calling:", "\uD83D\uDCF2"),
    DATE(":alarm_clock:", "\u23F0"),
    INTERVAL(":arrows_counterclockwise:", "\uD83D\uDD04"),
    IMAGE(":island:", "\uD83C\uDFDD"),
    CANCEL(":x:", "\u274C");

    private final String emoji;
    private final String unicode;

    EditOption(String emoji, String unicode) {
        this.emoji = emoji;
        this.unicode = unicode;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getUnicode() {
        return unicode;
    }

    public static EditOption getFromEmoji(String emoji) {
        return Arrays.stream(EditOption.values()).filter(option -> option.emoji.equalsIgnoreCase(emoji)).findFirst().orElse(null);
    }

    public static EditOption getFromUnicode(String unicode) {
        return Arrays.stream(EditOption.values()).filter(option -> option.unicode.equals(unicode)).findFirst().orElse(null);
    }
}
