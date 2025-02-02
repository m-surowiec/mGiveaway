package me.msuro.mGiveaway;

import me.msuro.mGiveaway.utils.ConfigUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class DiscordCommand extends ListenerAdapter {

    public DiscordCommand(GuildReadyEvent event) {
        MGiveaway instance = MGiveaway.getInstance();
        instance.getDiscordUtil().getJDA().addEventListener(this);
        String commandName = ConfigUtil.getAndValidate(ConfigUtil.COMMAND_NAME);
        String commandDesc = ConfigUtil.getAndValidate(ConfigUtil.COMMAND_DESCRIPTION);
        // v0.5 -> Make command options description configurable
        // v0.5 -> switch `prize_placeholder` to `minecraft_prize`
        SlashCommandData c = Commands
                .slash(commandName, commandDesc)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addOption(OptionType.STRING, "name", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_NAME), true)
                .addOption(OptionType.STRING, "prize", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_PRIZE), true)
                .addOption(OptionType.STRING, "minecraft_prize", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_MINECRAFT_PRIZE), true)
                .addOption(OptionType.STRING, "duration", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_DURATION), true)
                .addOption(OptionType.INTEGER, "winners", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_WINNERS), true)
                .addOption(OptionType.STRING, "command", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_COMMAND), true)
                .addOption(OptionType.BOOLEAN, "requirements", ConfigUtil.getAndValidate(ConfigUtil.DISCORD_OPTIONS_REQUIREMENTS), false);

        Guild guild = event.getGuild();
        guild.upsertCommand(c).queue();
    }


}
