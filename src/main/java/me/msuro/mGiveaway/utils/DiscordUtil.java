package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.classes.Giveaway;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class DiscordUtil {

    private final MGiveaway instance;

    private JDA jda;

    public DiscordUtil() {
        instance = MGiveaway.getInstance();
    }

    public JDA getJDA() {
        return jda;
    }

    public void build() {
        String token = ConfigUtil.getAndValidate(ConfigUtil.TOKEN);
        // "XXX" is the default value for all config values
        if (token.equalsIgnoreCase("XXX")) {
            instance.getLogger().severe("Bot token not set!");
            MGiveaway.setPaused(true);
            instance.getLogger().severe(ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_PLUGIN_PAUSED));
            return;
        }
        try {
            jda = JDABuilder.createDefault(token).setActivity(Activity.playing("with giveaways")).setStatus(OnlineStatus.ONLINE).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        } catch (InvalidTokenException e) {
            instance.getLogger().severe("Failed to start Discord bot " + e.getMessage());
            MGiveaway.setPaused(true);
            instance.getLogger().severe(ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_PLUGIN_PAUSED));
            return;
        }
        jda.getPresence().setActivity(getActivity());
        jda.getPresence().setStatus(getStatus());
        logActivity();
        logStatus();
        // todo STATUS updating doesn't work!!
    }


    private Activity getActivity() {
        String activity = ConfigUtil.getAndValidate(ConfigUtil.ACTIVITY);
        String activityText = ConfigUtil.getAndValidate(ConfigUtil.ACTIVITY_TEXT);
        String activityUrl = ConfigUtil.getAndValidate(ConfigUtil.ACTIVITY_URL);

        return switch (activity) {
            case "PLAYING" -> Activity.playing(activityText);
            case "WATCHING" -> Activity.watching(activityText);
            case "LISTENING" -> Activity.listening(activityText);
            case "STREAMING" -> Activity.streaming(activityText, activityUrl);
            default -> Activity.playing("Minecraft");
        };
    }

    private OnlineStatus getStatus() {
        String status = ConfigUtil.getAndValidate(ConfigUtil.STATUS);
        return switch (status) {
            case "IDLE", "AFK" -> OnlineStatus.IDLE;
            case "DO_NOT_DISTURB", "DND" -> OnlineStatus.DO_NOT_DISTURB;
            case "INVISIBLE", "OFFLINE" -> OnlineStatus.INVISIBLE;
            default -> OnlineStatus.ONLINE;
        };
    }

    public void logActivity() {
        instance.getServer().getConsoleSender().sendMessage(TextUtil.process("[mGiveaway] Activity set to: &7" + Objects.requireNonNull(jda.getPresence().getActivity()).getName()));
    }

    public void logStatus() {
        String color = switch (jda.getPresence().getStatus()) {
            case ONLINE -> "&a&l";
            case IDLE -> "&e&l";
            case DO_NOT_DISTURB -> "&c&l";
            default -> "&7&l";
        };
        instance.getServer().getConsoleSender().sendMessage(TextUtil.process("[mGiveaway] Status set to: " + color + jda.getPresence().getStatus()));
    }


    /**
     * Sends an embed to the Discord channel
     *
     * @param giveaway {@link Giveaway} object
     * @return message id
     */
    public String sendGiveawayEmbed(Giveaway giveaway) {
        EmbedBuilder eb = getEmbedBuilderFromConfig(giveaway, 1);
        MessageEmbed embed = eb.build();
        TextChannel tc = jda.getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            return Objects.requireNonNull(tc.sendMessageEmbeds(embed).addActionRow(getButton(giveaway)).complete()).getId();
        } else {
            instance.getLogger().severe("Giveaway channel not found!");
        }
        return "-1";
    }

    public String replaceJsonPlaceholders(String json, Giveaway giveaway) {
        // GLOBAL REPLACEMENTS - PLACEHOLDERS: {TIME-LEFT}, {ENTRIES}, {WIN-COUNT}, {PRIZE}, {END-TIME}
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        json = json.replace("{TIME-LEFT}", "<t:" + giveaway.endTimeParsed().toEpochSecond(ZoneOffset.UTC) + ":R>");
        json = json.replace("{END-TIME}", giveaway.endTime());
        json = json.replace("{ENTRIES}", giveaway.entries().size() + "");
        json = json.replace("{WIN-COUNT}", giveaway.winCount().toString());
        json = json.replace("{PRIZE}", giveaway.prize());

        // END GIVEAWAY EMBED REPLACEMENTS - PLACEHOLDER: {WINNERS}
        StringBuilder sb = new StringBuilder(" ");
        HashMap<String, String> entries = giveaway.entries();
        for (String key : giveaway.winners().keySet()) {
            sb.append("<@")
                    .append(key)
                    .append(">" + ": ")
                    .append(entries.get(key).replace("_", "\\\\_"))
                    .append("\\n");

        }
        if(sb.length() > 2) {
            sb.delete(sb.length() - 2, sb.length());
            json = json.replace("{WINNERS}", sb.toString());
        }

        // LOG EMBED GIVEAWAY REPLACEMENTS - PLACEHOLDERS: {GIVEAWAY-NAME}, {ENTRIES-COUNT}, {PRIZE}, {COMMANDS}, {WINNERS-MENTIONS}, {ENTRIES-LIST}
        json = json.replace("{GIVEAWAY-NAME}", giveaway.name());
        json = json.replace("{ENTRIES-COUNT}", giveaway.entries().size() + "");
        json = json.replace("{PRIZE}", giveaway.prize());
        json = json.replace("{COMMANDS}", String.join(",", giveaway.prizeCommands()));
        sb = new StringBuilder(" ");
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


    /**
     * Returns an {@link EmbedBuilder} object from the config file
     *
     * @param giveaway {@link Giveaway} object
     * @param type     1 for giveaway, 2 for giveaway end, 3 for log
     * @return {@link EmbedBuilder} object
     */
    public EmbedBuilder getEmbedBuilderFromConfig(Giveaway giveaway, int type) {
        EmbedBuilder eb = new EmbedBuilder();
        String path = (type == 1 ? ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_EMBED) :
                type == 2 ? ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_END_EMBED) :
                        ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_LOG_EMBED));

        String json = replaceJsonPlaceholders(path, giveaway);
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

    public ItemComponent getButton(Giveaway giveaway) {
        String type = ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_BUTTON_JOIN_BUTTON_TYPE);
        String text = ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_BUTTON_JOIN_BUTTON_TEXT);
        String emoji = ConfigUtil.getOptional(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_BUTTON_JOIN_BUTTON_EMOJI);
        if(emoji == null || emoji.isEmpty()) {
            return switch (type) {
                case "SECONDARY" -> Button.secondary("giveaway_" + giveaway.name(), text);
                case "SUCCESS" -> Button.success("giveaway_" + giveaway.name(), text);
                case "DANGER" -> Button.danger("giveaway_" + giveaway.name(), text);
                default -> Button.primary("giveaway_" + giveaway.name(), text);
            };
        } else {
            Emoji emo = Emoji.fromUnicode(emoji);
            return switch (type) {
                case "SECONDARY" -> Button.secondary("giveaway_" + giveaway.name(), text).withEmoji(emo);
                case "SUCCESS" -> Button.success("giveaway_" + giveaway.name(), text).withEmoji(emo);
                case "DANGER" -> Button.danger("giveaway_" + giveaway.name(), text).withEmoji(emo);
                default -> Button.primary("giveaway_" + giveaway.name(), text).withEmoji(emo);
            };
        }
    }

    public Modal getJoinForm(Giveaway giveaway) {
        ItemComponent ic = TextInput.create("nick", ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_MODAL_NICK_INPUT_QUESTION), TextInputStyle.SHORT)
                .setPlaceholder(ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_MODAL_NICK_INPUT_PLACEHOLDER))
                .setMinLength(3)
                .setMaxLength(16)
                .setRequired(true)
                .build();
        return Modal.create("giveaway_" + giveaway.name(), ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_MODAL_JOIN_MODAL_TITLE))
                .addActionRow(ic)
                .build();
    }

    public MessageEmbed getGiveawayEmbed(Giveaway giveaway) {
        TextChannel tc = jda.getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            return Objects.requireNonNull(tc.retrieveMessageById(giveaway.embedId()).complete()).getEmbeds().get(0);
        } else {
            instance.getLogger().severe("Giveaway channel not found!");
        }
        return null;
    }

    public void sendGiveawayEndEmbed(Giveaway giveaway, HashMap<String, String> winners) {
        EmbedBuilder eb = getEmbedBuilderFromConfig(giveaway, 2);
        StringBuilder sb = new StringBuilder(" ");
        for (String winner : winners.keySet()) {
            sb.append("<@").append(winner).append("> ");
        }
        TextChannel tc = jda.getTextChannelById(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_CHANNEL));
        if (tc != null) {
            Message embed = tc.retrieveMessageById(giveaway.embedId()).complete();
            embed.replyEmbeds(eb.build()).queue();
            if(!sb.isEmpty()) {
                String id = tc.sendMessage(sb.toString()).complete().getId();
                tc.deleteMessageById(id).queue();
            }
        } else {
            instance.getLogger().severe("Giveaway channel not found!");
        }
    }

}
