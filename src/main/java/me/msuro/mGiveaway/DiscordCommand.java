package me.msuro.mGiveaway;

import me.msuro.mGiveaway.utils.ConfigUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.command.Command;

public class DiscordCommand extends ListenerAdapter {

    private final MGiveaway instance;

    public DiscordCommand(GuildReadyEvent event) {
        this.instance = MGiveaway.getInstance();
        instance.getDiscordUtil().getJDA().addEventListener(this);
        String commandName = ConfigUtil.getAndValidate(ConfigUtil.COMMAND_NAME);
        String commandDesc = ConfigUtil.getAndValidate(ConfigUtil.COMMAND_DESCRIPTION);
        SlashCommandData c = Commands
                .slash(commandName, commandDesc)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addOption(OptionType.STRING, "name", "The name of the giveaway", true)
                .addOption(OptionType.STRING, "prize", "Formatted name of the prize", true)
                .addOption(OptionType.STRING, "prize_placeholder", "Placeholder for the prize displayed in the in-game messages", true)
                .addOption(OptionType.STRING, "duration", "The duration of the giveaway (1mo 2w 7d 5m 3s)", true)
                .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                .addOption(OptionType.STRING, "command", "First command to execute (if you want to run multiple commands, add them in config)", true)
                .addOption(OptionType.BOOLEAN, "requirements", "If the giveaway should wait for you to add requirements", false);

        Guild guild = event.getGuild();
        guild.upsertCommand(c).queue();
    }


}
