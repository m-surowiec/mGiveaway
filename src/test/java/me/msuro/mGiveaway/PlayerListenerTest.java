package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.msuro.mGiveaway.listener.PlayerListener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerListenerTest {

    private MGiveaway plugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        player = new PlayerMock(server, "TestPlayer");
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPlayerJoinEvent() {
        PlayerListener listener = new PlayerListener();
        PlayerJoinEvent event = new PlayerJoinEvent(player, "Welcome to the server!");
        server.getPluginManager().callEvent(event);
        // Add assertions to verify the behavior of the event handling
    }
}
