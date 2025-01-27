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

import java.util.List;
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
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(!event.getName().equalsIgnoreCase(ConfigUtil.getAndValidate(ConfigUtil.COMMAND_NAME))) return;
        if(!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You don't have permission to use this command!").setEphemeral(true).queue();
            return;
        }
        String name = event.getOption("name").getAsString();
        String prize = event.getOption("prize").getAsString();
        String prizePlaceholder = event.getOption("prize_placeholder").getAsString();
        String duration = event.getOption("duration").getAsString();
        int winners = Math.toIntExact(event.getOption("winners").getAsLong());
        String command = event.getOption("command").getAsString();
        boolean requirements = event.getOption("requirements") != null ? event.getOption("requirements").getAsBoolean() : false;

        if(!ConfigUtil.createGiveaway(name, prize, prizePlaceholder, duration, winners, command, requirements)) {
            event.reply("Giveaway with this name already exists!").setEphemeral(true).queue();
        } else {
            event.reply("Giveaway created successfully!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMessage().getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId())) return;
        if(!event.getComponentId().startsWith("giveaway_")) return;
        Giveaway giveaway = instance.getGiveaway(event.getComponentId().substring(9));
        if(giveaway == null) return;
        if(giveaway.hasEnded()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "Ten giveaway już się zakończył!")).setEphemeral(true).queue();
            return;
        }
        if(!giveaway.isStarted()) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "Ten giveaway jeszcze się nie zaczął!")).setEphemeral(true).queue();
            return;
        }
        if(giveaway.getEntryMap().containsKey(event.getUser().getId())) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "Już bierzesz udział w tym giveawayu jako " + giveaway.getEntryMap().get(event.getUser().getId()) + "!")).setEphemeral(true).queue();
            return;
        }
        Modal modal = instance.getDiscordUtil().getJoinForm(giveaway);
        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if(!Objects.requireNonNull(event.getMessage()).getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId())) return;
        if(!event.getModalId().startsWith("join_giveaway_")) return;
        Giveaway giveaway = new Giveaway(instance).fromConfig(event.getModalId().substring(14));
        if(giveaway == null) return;
        String nick = Objects.requireNonNull(event.getValue("nick")).getAsString();
        if(giveaway.getEntryMap().containsValue(nick)) {
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "Ten nick już bierze udział w giveawayu!")).setEphemeral(true).queue();
            return;
        }
        List<Requirement> requirements = giveaway.checkRequirements(nick);
        if(!requirements.isEmpty()) {
            if(requirements.getFirst().type() == Requirement.Type.NULLPLAYER) {
                event.replyEmbeds(TextUtil.getReplyEmbed(false, "**Nie wszedłeś nigdy na serwer!**")).setEphemeral(true).queue();
                return;
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(Requirement requirement : requirements) {
                if(requirement.getFormatted() == null || requirement.getFormatted().isEmpty() || requirement.getFormatted().equalsIgnoreCase("null")) continue;
                if(i < requirements.size() - 1) sb.append(requirement.getFormatted()).append(", ");
                else sb.append(requirement.getFormatted());
                i++;
            }
            event.replyEmbeds(TextUtil.getReplyEmbed(false, "**Nie spełniasz wymagań aby wziąć udział w giveawayu!** \n" + sb)).setEphemeral(true).queue();
            return;
        }
        giveaway.addEntry(event.getUser().getId(), nick);
        event.replyEmbeds(TextUtil.getReplyEmbed(true, "Zapisano nick! (Nick: " + nick + ")")).setEphemeral(true).queue();
        ConfigUtil.updateStat(event.getUser().getId(), 1);
        MessageEmbed embed = instance.getDiscordUtil().getEmbedBuilderFromConfig(giveaway, 1).build();
        event.getChannel().editMessageEmbedsById(giveaway.getEmbedId(), embed).queue();
    }
}