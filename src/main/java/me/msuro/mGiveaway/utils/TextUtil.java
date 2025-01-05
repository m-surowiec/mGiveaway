package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.utils.colors.ColorAPI;

public class TextUtil {

    private static MGiveaway instance;

    public TextUtil() {
        instance = MGiveaway.getInstance();
    }

    /**
     * Replaces color codes in a string with the corresponding color.
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        return text == null ? null : ColorAPI.process(text);
    }

    public static String process(String text) {
        if (text == null || text.isBlank()) return "null";
        text = color(text);
        return text;
    }
}