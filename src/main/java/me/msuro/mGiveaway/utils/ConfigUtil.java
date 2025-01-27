package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtil {

    private static MGiveaway instance;
    private static YamlConfiguration config;

    public ConfigUtil() {
        instance = MGiveaway.getInstance();

        instance.saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "config.yml"));

        if(!new File(instance.getDataFolder(), "config.yml").exists()) {
            instance.saveResource("config.yml", false);
            instance.getLogger().info("Config file created!");
        } else {
            if (config.getKeys(false).isEmpty()) {
                instance.getLogger().warning("Config file is empty! Please fill it with the required values.");
            } else {
                instance.getLogger().info("Config file loaded successfully!");
            }
        }

    }

    public static void saveConfig() {
        try {
            config.save(new File(instance.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save config file!", e);
        }
        instance.reloadConfig();
    }

    public void saveConfig(YamlConfiguration config) {
        try {
            config.save(new File(instance.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = config;
        instance.reloadConfig();
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static void reloadConfig() {
        instance.reloadConfig();
        config = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "config.yml"));
    }

    public static String getPrefix() {
        return instance.getConfig().getString("prefix");
    }

    /**
     * Gets a value from the config file and validates it.
     * Reports a warning if the value is set to default "XXX".
     * Returns "null" if the value is not set. (Sends an error message)
     *
     * @param key The key of the value to get
     * @return The value if it exists, "null" otherwise
     */
    public static String getAndValidate(String key) {
        String value = config.getString(key);
        if (value == null) {
            instance.getLogger().severe("Config value " + key + " not found!");
            value = "null";
        } else if (value.equals("XXX")) {
            instance.getLogger().warning("Config value " + key + " not set!");
        }
        return value;
    }

    public static String getOptional(String key) {
        return config.getString(key);
    }

    public static Integer getInt(String key) {
        int value = config.getInt(key, -2147483648);
        if (value == -2147483648) {
            instance.getLogger().severe("Config value " + key + " not found!");
        }
        return value;
    }

    /**
     * Updates the stats for a user
     *
     * @param userId The user's ID
     * @param type   1 for entered, 2 for won
     */
    public static void updateStat(String userId, int type) {
        String path = (type == 2 ? STAT_WON : STAT_ENTERED).replace("%s", userId);
        int value = config.getInt(path, 0);
        config.set(path, value + 1);
        saveConfig();
    }

    public static boolean createGiveaway(String name, String prize, String prizePlaceholder, String duration, int winners, String command, boolean requirements) {
        if(getOptional("giveaways." + name + ".settings.end_time") != null) return false;
        config.createSection("giveaways." + name);
        long durationInSeconds = parseDuration(duration);
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(durationInSeconds);
        config.set("giveaways." + name + ".settings.end_time", endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        config.set("giveaways." + name + ".settings.winners", winners);
        config.set("giveaways." + name + ".settings.commands", List.of(command));
        config.set("giveaways." + name + ".settings.started", false);
        config.set("giveaways." + name + ".settings.prize_formatted", prize);
        config.set("giveaways." + name + ".settings.prize_placeholder", prizePlaceholder);

        if(!requirements) config.set(ConfigUtil.FORCE_START.replace("%s", name), true);

        saveConfig();
        reloadConfig();
        return true;
    }

    Map<String, Long> timeMultipliers = new HashMap<>();



    /**
     * Parses a duration string and returns the total duration in seconds.
     * The duration string should be in the format "1mo 2w 7d 5m 3s".
     * The supported units are months (mo), weeks (w), days (d), minutes (m), and seconds (s).
     * @param duration The duration string to parse
     * @return The total duration in seconds
     */
    public static long parseDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration must not be null or empty.");
        }

        duration = duration.trim().toLowerCase();

        Map<String, Long> timeMultipliers = new HashMap<>();
        timeMultipliers.put("mo", 30L * 24 * 60 * 60);
        timeMultipliers.put("w", 7L * 24 * 60 * 60);
        timeMultipliers.put("d", 24L * 60 * 60);
        timeMultipliers.put("h", 60L * 60);
        timeMultipliers.put("m", 60L);
        timeMultipliers.put("s", 1L);

        long totalSeconds = 0;

        // Improved regex to ensure only valid unit characters and capture groups
        Pattern pattern = Pattern.compile("^\\s*(\\d+(?:mo|w|d|h|m|s))(\\s+\\d+(?:mo|w|d|h|m|s))*\\s*$");
        Matcher matcher = pattern.matcher(duration);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + duration);
        }

        Pattern blockPattern = Pattern.compile("(\\d+)(mo|w|d|h|m|s)");
        Matcher blockMatcher = blockPattern.matcher(duration);

        while (blockMatcher.find()) {
            long value = Long.parseLong(blockMatcher.group(1));
            String unit = blockMatcher.group(2);


            if (!timeMultipliers.containsKey(unit)) {
                throw new IllegalArgumentException("Unknown time unit: " + unit);
            }

            try{
                totalSeconds = Math.addExact(totalSeconds, Math.multiplyExact(value,timeMultipliers.get(unit)));
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Duration value too large: " + value + unit, ex);
            }


        }

        return totalSeconds;
    }


    public static final String PREFIX = "prefix";
    public static final String BROADCAST_INTERVAL = "broadcast_interval";
    public static final String BROADCAST_MESSAGE = "broadcast_message";

    public static final String TOKEN = "discord.bot.token";

    public static final String GIVEAWAY_EMBED = "discord.bot.giveaway_embed";
    public static final String GIVEAWAY_END_EMBED = "discord.bot.giveaway_end_embed";
    public static final String GIVEAWAY_CHANNEL = "discord.bot.giveaway_channel";

    public static final String LOG_EMBED_CHANNEL = "discord.bot.log_embed_channel";
    public static final String LOG_EMBED_COLOR = "discord.bot.log_embed_color";

    public static final String ACTIVITY = "discord.bot.activity";
    public static final String ACTIVITY_TEXT = "discord.bot.activity_text";
    public static final String ACTIVITY_URL = "discord.bot.activity_url";
    public static final String STATUS = "discord.bot.status";

    public static final String COMMAND_NAME = "discord.bot.command.name";
    public static final String COMMAND_DESCRIPTION = "discord.bot.command.description";


    public static final String ENTRIES = "entries.%s";

    public static final String STAT_ENTERED = "stats.%s.entered";
    public static final String STAT_WON = "stats.%s.won";

    public static final String SCH_START = "giveaways.%s.settings.scheduled_start";
    public static final String END_TIME = "giveaways.%s.settings.end_time";
    public static final String WINNERS = "giveaways.%s.settings.winners";
    public static final String COMMANDS = "giveaways.%s.settings.commands";
    public static final String STARTED = "giveaways.%s.settings.started";
    public static final String PRIZE_FORMATTED = "giveaways.%s.settings.prize_formatted";
    public static final String PRIZE_PLACEHOLDER = "giveaways.%s.settings.prize_placeholder";
    public static final String EMBED_ID = "giveaways.%s.settings.embed_id";
    public static final String FORCE_START = "giveaways.%s.settings.forcestart";
    public static final String ENDED = "giveaways.%s.ended";

    public static final String REQUIREMENT_PERMISSION = "giveaways.%s.requirements.permission";
    public static final String REQUIREMENT_GROUP = "giveaways.%s.requirements.group";
    public static final String REQUIREMENT_PLACEHOLDER = "giveaways.%s.requirements.placeholder";
    // %t = type, %r = requirement
    public static final String REQUIREMENT_FORMATTED = "giveaways.%s.requirements.%t.%r.formatted";




}
