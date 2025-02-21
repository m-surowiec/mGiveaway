package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.Giveaway;
import me.msuro.mGiveaway.utils.colors.ColorAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;

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

    public static MessageEmbed getReplyEmbed(boolean success, String description) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(success ? Color.GREEN : Color.RED);
        builder.setTitle((success ? ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_EMBED_TITLE_SUCCESS) : ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_EMBED_TITLE_ERROR)));
        builder.setDescription(description);
        return builder.build();
    }

    public static void sendLogEmbed(Giveaway giveaway) {
        TextChannel channel = instance.getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.LOG_EMBED_CHANNEL));
        if (channel == null) {
            throw new IllegalStateException("Log channel not found!");
        }
        MessageEmbed embed = instance.getDiscordUtil().getEmbedBuilderFromConfig(giveaway, 3).build();
        channel.sendMessageEmbeds(embed).queue();

    }
}