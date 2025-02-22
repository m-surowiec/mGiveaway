package me.msuro.mGiveaway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextUtilTest {

    @Test
    public void testColor() {
        String input = "&aHello &bWorld";
        String expected = "§aHello §bWorld";
        assertEquals(expected, TextUtil.color(input));
    }

    @Test
    public void testProcess() {
        String input = "%prefix% &aHello &bWorld";
        TextUtil.prefix = "&c[Prefix]";
        String expected = "§c[Prefix] §aHello §bWorld";
        assertEquals(expected, TextUtil.process(input));
    }

    @Test
    public void testToMinecraftHex() {
        String input = "&#00FF00Hello &#FF0000World";
        String expected = "§x§0§0§F§F§0§0Hello §x§F§F§0§0§0§0World";
        assertEquals(expected, TextUtil.toMinecraftHex(input));
    }

    @Test
    public void testReplaceJsonPlaceholders() {
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

        String json = "{ \"embed\": { \"title\": \"Giveaway: {GIVEAWAY-NAME}\", \"description\": \"Prize: {PRIZE}\" } }";
        String expected = "{ \"embed\": { \"title\": \"Giveaway: TestGiveaway\", \"description\": \"Prize: Test Prize\" } }";
        assertEquals(expected, TextUtil.replaceJsonPlaceholders(json, giveaway));
    }
}
