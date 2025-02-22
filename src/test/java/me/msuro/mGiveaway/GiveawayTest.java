package me.msuro.mGiveaway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GiveawayTest {

    private Giveaway giveaway;

    @BeforeEach
    void setUp() {
        giveaway = new Giveaway(
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
    }

    @Test
    void testGiveawayCreation() {
        assertNotNull(giveaway);
        assertEquals("TestGiveaway", giveaway.name());
        assertEquals("Test Prize", giveaway.prize());
        assertEquals("Test Minecraft Prize", giveaway.minecraftPrize());
        assertEquals("01/01/2023 12:00:00", giveaway.endTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), giveaway.endTimeParsed());
        assertEquals(1, giveaway.winCount());
        assertEquals(Giveaway.State.NOT_STARTED, giveaway.state());
        assertTrue(giveaway.entries().isEmpty());
        assertEquals(1, giveaway.prizeCommands().size());
        assertTrue(giveaway.winners().isEmpty());
        assertTrue(giveaway.requirements().isEmpty());
    }

    @Test
    void testGiveawayStateTransitions() {
        giveaway = giveaway.withState(Giveaway.State.STARTED);
        assertEquals(Giveaway.State.STARTED, giveaway.state());

        giveaway = giveaway.withState(Giveaway.State.ENDED);
        assertEquals(Giveaway.State.ENDED, giveaway.state());
    }

    @Test
    void testGiveawayHelperMethods() {
        assertFalse(giveaway.shouldStart());
        assertFalse(giveaway.shouldEnd());
        assertFalse(giveaway.hasEnded());

        giveaway = giveaway.withState(Giveaway.State.STARTED);
        assertTrue(giveaway.shouldEnd());

        giveaway = giveaway.withState(Giveaway.State.ENDED);
        assertTrue(giveaway.hasEnded());
    }
}
