package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.Giveaway;
import me.msuro.mGiveaway.MGiveaway;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Objects;

public class EmbedUtil {

    public static MessageEmbed getReplyEmbed(boolean success, String description) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(success ? Color.GREEN : Color.RED);
        builder.setTitle((success ? ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_EMBED_TITLE_SUCCESS) : ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_EMBED_TITLE_ERROR)));
        builder.setDescription(description);
        return builder.build();
    }

    public static void sendLogEmbed(Giveaway giveaway) {
        TextChannel channel = MGiveaway.getInstance().getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.LOG_EMBED_CHANNEL));
        if (channel == null) {
            throw new IllegalStateException("Log channel not found!");
        }
        MessageEmbed embed = getEmbedBuilderFromConfig(giveaway, 3).build();
        channel.sendMessageEmbeds(embed).queue();

    }

    /**
     * Sends an embed to the Discord channel
     *
     * @param giveaway {@link Giveaway} object
     * @return message id
     */
    public static String sendGiveawayEmbed(Giveaway giveaway) {
        EmbedBuilder eb = getEmbedBuilderFromConfig(giveaway, 1);
        MessageEmbed embed = eb.build();
        TextChannel tc = MGiveaway.getInstance().getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            return Objects.requireNonNull(tc.sendMessageEmbeds(embed).addActionRow(MGiveaway.getInstance().getDiscordUtil().getButton(giveaway)).complete()).getId();
        } else {
            MGiveaway.getInstance().getLogger().severe("Giveaway channel not found!");
        }
        return "-1";
    }



    /**
     * Returns an {@link EmbedBuilder} object from the config file
     *
     * @param giveaway {@link Giveaway} object
     * @param type     1 for giveaway, 2 for giveaway end, 3 for log
     * @return {@link EmbedBuilder} object
     */
    public static EmbedBuilder getEmbedBuilderFromConfig(Giveaway giveaway, int type) {
        EmbedBuilder eb = new EmbedBuilder();
        String path = (type == 1 ? ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_EMBED) :
                type == 2 ? ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_END_EMBED) :
                        ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_LOG_EMBED));

        String json = TextUtil.replaceJsonPlaceholders(path, giveaway);
        JSONObject messageObj = new JSONObject(json);
        JSONObject embedObj = messageObj.getJSONObject("embed");
        eb.setTitle(embedObj.has("title") ? embedObj.getString("title") : null, embedObj.has("url") ? embedObj.getString("url") : null);
        eb.setDescription(embedObj.has("description") ? embedObj.getString("description") : null);
        eb.setColor(Color.decode(embedObj.getString("color")));
        if (embedObj.has("timestamp")) {
            eb.setTimestamp(OffsetDateTime.parse(embedObj.getString("timestamp")));
        }
        if (embedObj.has("footer")) {
            JSONObject footerObj = embedObj.getJSONObject("footer");
            eb.setFooter(footerObj.has("text") ? footerObj.getString("text") : null, footerObj.has("icon_url") ? footerObj.getString("icon_url") : null);
        }
        if (embedObj.has("thumbnail")) {
            eb.setThumbnail(embedObj.getJSONObject("thumbnail").has("url") ? embedObj.getJSONObject("thumbnail").getString("url") : null);
        }
        if (embedObj.has("image")) {
            eb.setImage(embedObj.getJSONObject("image").has("url") ? embedObj.getJSONObject("image").getString("url") : null);
        }
        if (embedObj.has("author")) {
            JSONObject authorObj = embedObj.getJSONObject("author");
            eb.setAuthor(authorObj.has("name") ? authorObj.getString("name") : null, authorObj.has("url") ? authorObj.getString("url") : null, authorObj.has("icon_url") ? authorObj.getString("icon_url") : null);
        }
        if (embedObj.has("fields")) {
            JSONArray fields = embedObj.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                eb.addField(field.has("name") ? field.getString("name") : "null", field.has("value") ? field.getString("value") : "null", field.optBoolean("inline", false));
            }
        }
        return eb;

    }

    public MessageEmbed getGiveawayEmbed(Giveaway giveaway) {
        TextChannel tc = MGiveaway.getInstance().getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            return Objects.requireNonNull(tc.retrieveMessageById(giveaway.embedId()).complete()).getEmbeds().get(0);
        } else {
            MGiveaway.getInstance().getLogger().severe("Giveaway channel not found!");
        }
        return null;
    }

    public static void sendGiveawayEndEmbed(Giveaway giveaway, HashMap<String, String> winners) {
        EmbedBuilder eb = getEmbedBuilderFromConfig(giveaway, 2);
        StringBuilder sb = new StringBuilder();
        for (String winner : winners.keySet()) {
            sb.append("<@").append(winner).append("> ");
        }
        TextChannel tc = MGiveaway.getInstance().getDiscordUtil().getJDA().getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            Message embed = tc.retrieveMessageById(giveaway.embedId()).complete();
            embed.replyEmbeds(eb.build()).queue();
            if(!sb.toString().isEmpty()) {
                String id = tc.sendMessage(sb.toString()).complete().getId();
                tc.deleteMessageById(id).queue();
            }
        } else {
            MGiveaway.getInstance().getLogger().severe("Giveaway channel not found!");
        }
    }

}
