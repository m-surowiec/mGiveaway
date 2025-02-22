package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.msuro.mGiveaway.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbedUtilTest {

    private MGiveaway plugin;
    private TextChannel textChannel;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        textChannel = mock(TextChannel.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testGetReplyEmbed() {
        MessageEmbed successEmbed = EmbedUtil.getReplyEmbed(true, "Success message");
        assertNotNull(successEmbed);
        assertEquals("Success", successEmbed.getTitle());
        assertEquals("Success message", successEmbed.getDescription());

        MessageEmbed errorEmbed = EmbedUtil.getReplyEmbed(false, "Error message");
        assertNotNull(errorEmbed);
        assertEquals("Error", errorEmbed.getTitle());
        assertEquals("Error message", errorEmbed.getDescription());
    }

    @Test
    public void testSendLogEmbed() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                null,
                null,
                null,
                1,
                null,
                Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                null,
                new HashMap<>(),
                null
        );

        EmbedUtil.sendLogEmbed(giveaway);
        verify(textChannel, times(1)).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    public void testSendGiveawayEmbed() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                null,
                null,
                null,
                1,
                null,
                Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                null,
                new HashMap<>(),
                null
        );

        String messageId = EmbedUtil.sendGiveawayEmbed(giveaway);
        assertNotNull(messageId);
        verify(textChannel, times(1)).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    public void testSendGiveawayEndEmbed() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                null,
                null,
                null,
                1,
                null,
                Giveaway.State.ENDED,
                new HashMap<>(),
                null,
                new HashMap<>(),
                null
        );

        HashMap<String, String> winners = new HashMap<>();
        winners.put("123456789", "TestPlayer");

        EmbedUtil.sendGiveawayEndEmbed(giveaway, winners);
        verify(textChannel, times(1)).sendMessageEmbeds(any(MessageEmbed.class));
    }
}
