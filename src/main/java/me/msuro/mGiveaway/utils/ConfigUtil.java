package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

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


    public static final String PREFIX = "prefix";

    public static final String TOKEN = "discord.bot.token";

    public static final String GIVEAWAY_EMBED = "discord.bot.giveaway_embed";
    public static final String GIVEAWAY_END_EMBED = "discord.bot.giveaway_end_embed";
    public static final String GIVEAWAY_CHANNEL = "discord.bot.giveaway_channel";

    public static final String ACTIVITY = "discord.bot.activity";
    public static final String ACTIVITY_TEXT = "discord.bot.activity_text";
    public static final String ACTIVITY_URL = "discord.bot.activity_url";
    public static final String STATUS = "discord.bot.status";

    public static final String ENTRIES = "entries.%s";

    public static final String STAT_ENTERED = "stats.%s.entered";
    public static final String STAT_WON = "stats.%s.won";

    public static final String SCH_START = "giveaways.%s.settings.scheduled_start";
    public static final String END_TIME = "giveaways.%s.settings.end_time";
    public static final String WINNERS = "giveaways.%s.settings.winners";
    public static final String COMMAND = "giveaways.%s.settings.command";
    public static final String STARTED = "giveaways.%s.settings.started";
    public static final String PRIZE_FORMATTED = "giveaways.%s.settings.prize_formatted";
    public static final String EMBED_ID = "giveaways.%s.settings.embed_id";

    public static final String REQUIREMENT_PERMISSION = "giveaways.%s.requirements.permission";
    public static final String REQUIREMENT_GROUP = "giveaways.%s.requirements.group";
    public static final String REQUIREMENT_PLACEHOLDER = "giveaways.%s.requirements.placeholder";




}
