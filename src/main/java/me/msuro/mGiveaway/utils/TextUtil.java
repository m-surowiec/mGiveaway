package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.Giveaway;
import me.msuro.mGiveaway.utils.colors.ColorAPI;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static MGiveaway instance;
    public static String prefix;

    public static void setInstance(MGiveaway instance) {
        TextUtil.instance = instance;
    }

    /**
     * Replaces color codes in a string with the corresponding color.
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        return text == null ? null : ColorAPI.process(text);
    }

    public static String process(String text) {
        if (text == null || text.isBlank()) return "null";
        text = text.replace("%prefix%", prefix);
        text = color(text);
        return text;
    }

    /**
     * Replaces & with § and &#HHHHHH with §x§H§H§H§H§H§H
     * Used for UpdateChecker messages which require {@link net.md_5.bungee.api.chat.BaseComponent} to be sent
     * @param text The text to convert
     * @return The converted text
     */
    public static String toMinecraftHex(String text) {
        Pattern pattern = Pattern.compile("&#([0-9A-Fa-f]{6})");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "§x" + "§" + hex.charAt(0)
                    + "§" + hex.charAt(1)
                    + "§" + hex.charAt(2)
                    + "§" + hex.charAt(3)
                    + "§" + hex.charAt(4)
                    + "§" + hex.charAt(5);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString().replace("&", "§");
    }


    public static String replaceJsonPlaceholders(String json, Giveaway giveaway) {
        // GLOBAL REPLACEMENTS - PLACEHOLDERS: {TIME-LEFT}, {ENTRIES}, {WIN-COUNT}, {PRIZE}, {END-TIME}
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        json = json.replace("{TIME-LEFT}",
                "<t:" + giveaway.endTimeParsed()
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond() + ":R>");        json = json.replace("{END-TIME}", giveaway.endTime());
        json = json.replace("{ENTRIES}", giveaway.entries().size() + "");
        json = json.replace("{WIN-COUNT}", giveaway.winCount().toString());
        json = json.replace("{PRIZE}", giveaway.prize());

        HashMap<String, String> entries = giveaway.entries();

        // END GIVEAWAY EMBED REPLACEMENTS - PLACEHOLDER: {WINNERS}
        if(giveaway.state() == Giveaway.State.ENDED) {
            StringBuilder sb = new StringBuilder(" ");
            for (String key : giveaway.winners().keySet()) {
                sb.append("<@")
                        .append(key)
                        .append(">" + ": ")
                        .append(entries.get(key).replace("_", "\\\\_"))
                        .append("\\n");

            }
            if (sb.length() > 2) {
                sb.delete(sb.length() - 2, sb.length());
                json = json.replace("{WINNERS}", sb.toString());
            } else {
                json = json.replace("{WINNERS}", "No winners!");
            }
        }

        // LOG EMBED GIVEAWAY REPLACEMENTS - PLACEHOLDERS: {GIVEAWAY-NAME}, {ENTRIES-COUNT}, {PRIZE}, {COMMANDS}, {WINNERS-MENTIONS}, {ENTRIES-LIST}
        json = json.replace("{GIVEAWAY-NAME}", giveaway.name());
        json = json.replace("{ENTRIES-COUNT}", giveaway.entries().size() + "");
        json = json.replace("{PRIZE}", giveaway.prize());
        json = json.replace("{COMMANDS}", String.join(",", giveaway.prizeCommands()));
        StringBuilder sb = new StringBuilder(" ");
        for (String key : giveaway.winners().keySet()) {
            sb.append("<@").append(key).append("> ");
        }
        json = json.replace("{WINNERS-MENTIONS}", sb.toString());
        sb = new StringBuilder(" ");
        for (String entry : entries.keySet()) {
            sb.append("<@").append(entry).append(">: ").append(entries.get(entry).replace("_", "\\\\_")).append("\\n");
        }
        json = json.replace("{ENTRIES-LIST}", sb.toString());




        return json;
    }
}