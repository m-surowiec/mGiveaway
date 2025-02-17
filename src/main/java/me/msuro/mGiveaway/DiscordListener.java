package me.msuro.mGiveaway;

import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.classes.Requirement;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.TextUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DiscordListener extends ListenerAdapter {

    private final MGiveaway instance;

    public DiscordListener() {
        this.instance = MGiveaway.getInstance();
        instance.getDiscordUtil().getJDA().addEventListener(this);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        new DiscordCommand(event);
        instance.getLogger().info("Discord command registered! [" + event.getGuild().getName() + "]");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(!event.getName().equalsIgnoreCase(ConfigUtil.getAndValidate(ConfigUtil.COMMAND_NAME))) return;
        if(!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_NO_PERMISSION))).setEphemeral(true).queue();
            return;
        }
        if(MGiveaway.isPaused()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_PLUGIN_PAUSED))).setEphemeral(true).queue();
            return;
        }
        String name = event.getOption("name").getAsString();
        String prize = event.getOption("prize").getAsString();
        String minecraftPrize = event.getOption("minecraft_prize").getAsString();
        String duration = event.getOption("duration").getAsString();
        int winners = Math.toIntExact(event.getOption("winners").getAsLong());
        String command = event.getOption("command").getAsString();
        boolean requirements = event.getOption("requirements") != null ? event.getOption("requirements").getAsBoolean() : false;

        if(!ConfigUtil.createGiveaway(name, prize, minecraftPrize, duration, winners, command, requirements)) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "Giveaway with this name already exists!")).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(TextUtil.getReplyEmbed(true, "Giveaway created successfully!")).setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMessage().getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId())) return;
        if(!event.getComponentId().startsWith("giveaway_")) return;
        if(MGiveaway.isPaused()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_PLUGIN_PAUSED))).setEphemeral(true).queue();
            return;
        }
        Giveaway giveaway = instance.getGiveaway(event.getComponentId().substring(9));
        if(giveaway == null) return;
        if(giveaway.hasEnded()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_ALREADY_ENDED))).setEphemeral(true).queue();
            return;
        }
        if(giveaway.state() != Giveaway.State.STARTED) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_NOT_STARTED))).setEphemeral(true).queue();
            return;
        }
        if(giveaway.entries().containsKey(event.getUser().getId())) {
            String msg = ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_ALREADY_JOINED).replace("%player%", giveaway.entries().get(event.getUser().getId()));
            event.replyEmbeds(TextUtil.getReplyEmbed(false, msg)).setEphemeral(true).queue();
            return;
        }
        Modal modal = instance.getDiscordUtil().getJoinForm(giveaway);
        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!Objects.requireNonNull(event.getMessage()).getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId()))
            return;
        if (!event.getModalId().startsWith("giveaway_")) return;
        if (MGiveaway.isPaused()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_COMMAND_ERROR_PLUGIN_PAUSED))).setEphemeral(true).queue();
            return;
        }
        Giveaway giveaway = instance.getGiveaway(event.getModalId().substring(9));
        if (giveaway == null) return;
        // Check if giveaway has ended
        if(giveaway.state() == Giveaway.State.ENDED) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_ALREADY_ENDED))).setEphemeral(true).queue();
            return;
        }
        // Check if giveaway has started
        if(giveaway.state() != Giveaway.State.STARTED) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_NOT_STARTED))).setEphemeral(true).queue();
            return;
        }
        // Check if the provided nick is already joined
        String nick = Objects.requireNonNull(event.getValue("nick")).getAsString();
        if (giveaway.entries().containsValue(nick)) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_NICK_ALREADY_JOINED))).setEphemeral(true).queue();
            return;
        }
        // Check if this discord user is already joined
        if(giveaway.entries().containsKey(event.getUser().getId())) {
            String msg = ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_JOIN_ALREADY_JOINED).replace("%player%", giveaway.entries().get(event.getUser().getId()));
            event.replyEmbeds(TextUtil.getReplyEmbed(false, msg)).setEphemeral(true).queue();
            return;
        }
        // Check if the player meets the requirements
        instance.getGiveawayManager().checkRequirementsAsync(giveaway, nick, unmetRequirements -> {
            if (unmetRequirements == null) {
                instance.getLogger().warning("Error during asynchronous player lookup for " + nick + " in giveaway " + giveaway.name());
                event.replyEmbeds(TextUtil.getReplyEmbed(false, "Error checking requirements. Please try again.")).setEphemeral(true).queue(); // Generic error message
                return;
            }

            if (unmetRequirements.isEmpty()) {
                // Player meets all requirements - proceed with entry
                giveaway.addEntry(event.getUser().getId(), nick);
                instance.getGiveawayManager().putGiveaway(giveaway);
                String msg = ConfigUtil.getAndValidate("messages.discord.giveaway_join.joined").replace("%player%", nick);
                event.replyEmbeds(TextUtil.getReplyEmbed(true, msg)).setEphemeral(true).queue();
                ConfigUtil.updateStat(event.getUser().getId(), 1);
                MessageEmbed embed = instance.getDiscordUtil().getEmbedBuilderFromConfig(giveaway, 1).build();
                event.getChannel().editMessageEmbedsById(giveaway.embedId(), embed).queue();

            } else {
                if (unmetRequirements.get(0).type() == Requirement.Type.NULLPLAYER) {
                    event.replyEmbeds(TextUtil.getReplyEmbed(false, ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_REQUIREMENT_ERROR_NULL_PLAYER))).setEphemeral(true).queue();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Requirement requirement : unmetRequirements) {
                    if (requirement.getFormatted() == null || requirement.getFormatted().isEmpty() || requirement.getFormatted().equalsIgnoreCase("null"))
                        continue;
                    if (i < unmetRequirements.size() - 1) sb.append(requirement.getFormatted()).append(", ");
                    else sb.append(requirement.getFormatted());
                    i++;
                }
                String msg = ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_DISCORD_GIVEAWAY_REQUIREMENT_ERROR_REQUIREMENTS_NOT_MET).replace("%requirements%", sb.toString());
                event.replyEmbeds(TextUtil.getReplyEmbed(false, msg)).setEphemeral(true).queue();
            }
        });
    }
}