package me.msuro.mGiveaway.utils.colors.patterns;

import me.msuro.mGiveaway.utils.colors.ColorAPI;

import java.util.regex.Matcher;

public class SolidPattern implements Pattern {

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<SOLID:([0-9A-Fa-f]{6})>|&#([0-9A-Fa-f]{6})");

    /**
     * Applies a solid RGB color to the provided String.
     * <SOLID:XXXXXX> or &#XXXXXX where X is a hexadecimal digit.
     * Output might be the same as the input if this pattern is not present.
     *
     * @param string The String to which this pattern should be applied to
     * @return The new String with applied pattern
     */
    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            if (color == null) color = matcher.group(2);

            string = string.replace(matcher.group(), ColorAPI.getColor(color) + "");
        }
        return string;
    }

}