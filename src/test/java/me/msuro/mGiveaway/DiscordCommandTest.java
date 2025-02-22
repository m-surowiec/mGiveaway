package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.msuro.mGiveaway.discord.DiscordCommand;
import me.msuro.mGiveaway.utils.ConfigUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscordCommandTest {

    private MGiveaway plugin;
    private GuildReadyEvent event;
    private Guild guild;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        guild = mock(Guild.class);
        event = mock(GuildReadyEvent.class);
        when(event.getGuild()).thenReturn(guild);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCommandRegistration() {
        new DiscordCommand(event);
        verify(guild).upsertCommand(any(SlashCommandData.class));
    }

    @Test
    public void testCommandExecution() {
        new DiscordCommand(event);
        // Add more specific tests for command execution if needed
    }
}
