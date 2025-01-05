package me.msuro.mGiveaway;

import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DiscordUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
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
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMessage().getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId())) return;
        if(!event.getComponentId().startsWith("giveaway_")) return;
        Giveaway giveaway = new Giveaway().fromConfig(event.getComponentId().substring(9));
        if(giveaway == null) return;
        if(giveaway.isEnded()) {
            event.reply("Ten giveaway już się zakończył!").setEphemeral(true).queue();
            return;
        }
        if(!giveaway.isStarted()) {
            event.reply("Ten giveaway jeszcze się nie zaczął!").setEphemeral(true).queue();
            return;
        }
        if(giveaway.getEntries().contains(event.getUser().getId())) {
            event.reply("Już bierzesz udział w tym giveawayu jako " + ConfigUtil.getAndValidate(ConfigUtil.ENTRIES.replace("%s", giveaway.getName() + "." + event.getUser().getId()))).setEphemeral(true).queue();
            return;
        }
        Modal modal = instance.getDiscordUtil().getJoinForm(giveaway);
        event.replyModal(modal).queue();

    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if(!Objects.requireNonNull(event.getMessage()).getAuthor().getId().equalsIgnoreCase(instance.getDiscordUtil().getJDA().getSelfUser().getId())) return;
        if(!event.getModalId().startsWith("join_giveaway_")) return;
        Giveaway giveaway = new Giveaway().fromConfig(event.getModalId().substring(14));
        if(giveaway == null) return;
        String nick = Objects.requireNonNull(event.getValue("nick")).getAsString();
        if(giveaway.getNickEntries().contains(nick)) {
            event.reply("Ten nick już bierze udział w giveawayu!").setEphemeral(true).queue();
            return;
        }
        ConfigUtil.getConfig().set(ConfigUtil.ENTRIES.replace("%s", giveaway.getName() + "." + event.getUser().getId()), nick);
        ConfigUtil.saveConfig();
        event.reply("Zapisano nick! (Nick: " + nick + ")").setEphemeral(true).queue();
        giveaway.refreshEntries();
        ConfigUtil.updateStat(event.getUser().getId(), 1);
        MessageEmbed embed = instance.getDiscordUtil().getEmbedBuilderFromConfig(giveaway, 1).build();
        event.getChannel().editMessageEmbedsById(giveaway.getEmbedId(), embed).queue();
    }
}
