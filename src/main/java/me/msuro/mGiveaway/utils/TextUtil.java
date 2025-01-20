package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.utils.colors.ColorAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;

public class TextUtil {

    private static MGiveaway instance;

    public TextUtil() {
        instance = MGiveaway.getInstance();
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
        text = color(text);
        return text;
    }

    public static MessageEmbed getReplyEmbed(boolean success, String description) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(success ? Color.GREEN : Color.RED);
        builder.setTitle((success ? "Udało się" : "Nie udało się") + "!");
        builder.setDescription(description);
        return builder.build();
    }

    public static void sendGiveawayEmbed(Giveaway giveaway) {
        TextChannel channel = instance.getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.LOG_EMBED_CHANNEL));
        if (channel == null) {
            throw new IllegalStateException("Log channel not found!");
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.getColor(ConfigUtil.getAndValidate(ConfigUtil.LOG_EMBED_COLOR)));
        builder.setTitle("Giveaway zakończony!");

        // I have an ewntryMap (userId, nick) and I want to stream it like this: "<@userId>: nick, <@userId>: nick, ..."
        String entries = giveaway.getEntryMap().entrySet().stream()
                .map(entry -> "<@" + entry.getKey() + ">: " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Brak");

        builder.setDescription(
                "Giveaway **" + giveaway.getName() + "** [" + giveaway.getEntries().size() + "]" + "\n" +
                        "- **Nagroda:** " + giveaway.getPrize() + " [" + String.join(",", giveaway.getCommands()) + "]\n" +
                        "- **Zwycięzcy:** " + giveaway.getWinners().stream().map(id -> "<@" + id + ">").reduce((a, b) -> a + ", " + b).orElse("Brak") + "\n" +
                        "- **Wejścia:** " + entries + "\n");



        channel.sendMessageEmbeds(builder.build()).queue();
    }
}