package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MGiveawayTest {

    private ServerMock server;
    private MGiveaway plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testPluginEnable() {
        assertTrue(plugin.isEnabled());
    }

    @Test
    void testPluginDisable() {
        MockBukkit.unload(plugin);
        assertFalse(plugin.isEnabled());
    }

    @Test
    void testCommandRegistration() {
        PluginManagerMock pluginManager = server.getPluginManager();
        Plugin commandPlugin = pluginManager.getPlugin("MGiveaway");
        assertNotNull(commandPlugin);
        assertTrue(commandPlugin.isEnabled());
    }

    @Test
    void testEventListeners() {
        PluginManagerMock pluginManager = server.getPluginManager();
        assertTrue(pluginManager.isListeningFor("PlayerJoinEvent"));
    }
}
