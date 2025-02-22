package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DBUtils;
import me.msuro.mGiveaway.utils.GiveawayManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GiveawayManagerTest {

    private MGiveaway plugin;
    private GiveawayManager giveawayManager;
    private DBUtils dbUtils;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        giveawayManager = new GiveawayManager();
        dbUtils = new DBUtils();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testGiveawayCreation() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                null,
                null,
                1,
                null,
                Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                List.of("test command"),
                new HashMap<>(),
                List.of()
        );
        giveawayManager.putGiveaway(giveaway);
        assertEquals(1, giveawayManager.listGiveaways().size());
        assertEquals(giveaway, giveawayManager.listGiveaways().get("TestGiveaway"));
    }

    @Test
    public void testGiveawayStarting() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                null,
                null,
                1,
                null,
                Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                List.of("test command"),
                new HashMap<>(),
                List.of()
        );
        giveawayManager.startGiveaway(giveaway);
        assertEquals(Giveaway.State.STARTED, giveawayManager.listGiveaways().get("TestGiveaway").state());
    }

    @Test
    public void testGiveawayEnding() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                null,
                null,
                1,
                null,
                Giveaway.State.STARTED,
                new HashMap<>(),
                List.of("test command"),
                new HashMap<>(),
                List.of()
        );
        giveawayManager.endGiveaway(giveaway);
        assertEquals(Giveaway.State.ENDED, giveawayManager.listGiveaways().get("TestGiveaway").state());
    }

    @Test
    public void testEntryManagement() {
        Giveaway giveaway = new Giveaway(
                "TestGiveaway",
                "Test Prize",
                "Test Minecraft Prize",
                "01/01/2023 12:00:00",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                null,
                null,
                1,
                null,
                Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                List.of("test command"),
                new HashMap<>(),
                List.of()
        );
        giveawayManager.putGiveaway(giveaway);
        giveawayManager.addEntry(giveaway, "testUser", "testNick");
        assertEquals(1, giveawayManager.listGiveaways().get("TestGiveaway").entries().size());
        assertEquals("testNick", giveawayManager.listGiveaways().get("TestGiveaway").entries().get("testUser"));
    }
}
