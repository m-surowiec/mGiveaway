package me.msuro.mGiveaway;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.managers.Presence;
import be.seeseemelk.mockbukkit.MockBukkit;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DiscordUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscordUtilTest {

    private MGiveaway plugin;
    private DiscordUtil discordUtil;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        discordUtil = new DiscordUtil();
        jda = mock(JDA.class);
        guild = mock(Guild.class);
        textChannel = mock(TextChannel.class);
        when(jda.getTextChannelById(anyString())).thenReturn(textChannel);
        when(jda.getGuildById(anyString())).thenReturn(guild);
        when(jda.getPresence()).thenReturn(mock(Presence.class));
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testDiscordBotInitialization() {
        discordUtil.build();
        assertNotNull(discordUtil.getJDA());
    }

    @Test
    public void testActivityUpdate() {
        discordUtil.build();
        discordUtil.getJDA().getPresence().setActivity(Activity.playing("Test Activity"));
        assertEquals("Test Activity", discordUtil.getJDA().getPresence().getActivity().getName());
    }

    @Test
    public void testStatusUpdate() {
        discordUtil.build();
        discordUtil.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
        assertEquals(OnlineStatus.IDLE, discordUtil.getJDA().getPresence().getStatus());
    }
}
