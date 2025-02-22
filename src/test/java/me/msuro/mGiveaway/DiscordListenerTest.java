package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.msuro.mGiveaway.discord.DiscordListener;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscordListenerTest {

    private MGiveaway plugin;
    private GuildReadyEvent guildReadyEvent;
    private SlashCommandInteractionEvent slashCommandInteractionEvent;
    private ButtonInteractionEvent buttonInteractionEvent;
    private ModalInteractionEvent modalInteractionEvent;
    private Guild guild;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        guild = mock(Guild.class);
        guildReadyEvent = mock(GuildReadyEvent.class);
        slashCommandInteractionEvent = mock(SlashCommandInteractionEvent.class);
        buttonInteractionEvent = mock(ButtonInteractionEvent.class);
        modalInteractionEvent = mock(ModalInteractionEvent.class);
        when(guildReadyEvent.getGuild()).thenReturn(guild);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testGuildReadyEventHandling() {
        DiscordListener listener = new DiscordListener();
        listener.onGuildReady(guildReadyEvent);
        verify(guildReadyEvent, times(1)).getGuild();
    }

    @Test
    public void testSlashCommandInteractionEventHandling() {
        DiscordListener listener = new DiscordListener();
        when(slashCommandInteractionEvent.getName()).thenReturn(ConfigUtil.getAndValidate(ConfigUtil.COMMAND_NAME));
        listener.onSlashCommandInteraction(slashCommandInteractionEvent);
        verify(slashCommandInteractionEvent, times(1)).getName();
    }

    @Test
    public void testButtonInteractionEventHandling() {
        DiscordListener listener = new DiscordListener();
        when(buttonInteractionEvent.getComponentId()).thenReturn("giveaway_test");
        listener.onButtonInteraction(buttonInteractionEvent);
        verify(buttonInteractionEvent, times(1)).getComponentId();
    }

    @Test
    public void testModalInteractionEventHandling() {
        DiscordListener listener = new DiscordListener();
        when(modalInteractionEvent.getModalId()).thenReturn("giveaway_test");
        listener.onModalInteraction(modalInteractionEvent);
        verify(modalInteractionEvent, times(1)).getModalId();
    }
}
