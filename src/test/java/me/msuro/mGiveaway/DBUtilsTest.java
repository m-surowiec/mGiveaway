package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.msuro.mGiveaway.utils.DBUtils;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class DBUtilsTest {

    private MGiveaway plugin;
    private DBUtils dbUtils;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        dbUtils = new DBUtils();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateGiveawayTable() {
        dbUtils.createGiveawayTable("testGiveaway");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/mgiveaway.sqlite");
             Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='entries-testGiveaway';");
            assertTrue(resultSet.next());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSaveEntry() {
        dbUtils.createGiveawayTable("testGiveaway");
        dbUtils.saveEntry("testGiveaway", "testDiscordId", "testMinecraftName");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/mgiveaway.sqlite");
             Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM `entries-testGiveaway` WHERE discord_id='testDiscordId' AND minecraft_name='testMinecraftName';");
            assertTrue(resultSet.next());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testRefreshEntries() {
        dbUtils.createGiveawayTable("testGiveaway");
        dbUtils.saveEntry("testGiveaway", "testDiscordId", "testMinecraftName");
        Giveaway giveaway = new Giveaway("testGiveaway", "testPrize", "testMinecraftPrize", "01/01/2023 00:00:00", null, null, null, 1, null, Giveaway.State.NOT_STARTED, new HashMap<>(), null, new HashMap<>(), null);
        HashMap<String, String> entries = dbUtils.refreshEntries(giveaway);
        assertEquals(1, entries.size());
        assertEquals("testMinecraftName", entries.get("testDiscordId"));
    }
}
