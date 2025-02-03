package me.msuro.mGiveaway;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.commands.Reload;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DBUtils;
import me.msuro.mGiveaway.utils.DiscordUtil;
import me.msuro.mGiveaway.utils.TextUtil;
import net.dv8tion.jda.api.JDA;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public final class MGiveaway extends JavaPlugin {

    private static MGiveaway instance;
    private DiscordUtil discordUtil;
    private DBUtils dbUtils;
    private static boolean pausePlugin = false;

    Metrics metrics;

    private BukkitTask updateGiveaways;
    private BukkitTask updateCheck;

    private final List<Giveaway> giveaways = new ArrayList<>();

    private Permission perms = null;

    private static final HashMap<Giveaway, HashMap<String, String>> entries = new HashMap<>();

    public HashMap<Giveaway, HashMap<String, String>> getEntries() {
        return entries;
    }

    public void addEntry(Giveaway giveaway, HashMap<String, String> entries) {
        HashMap<String, String> entry = MGiveaway.entries.get(giveaway);
        if(entry == null) {
            entry = new HashMap<>();
        }
        entry.putAll(entries);
        MGiveaway.entries.put(giveaway, entry);
    }

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getLogger().info("Enabling plugin...");

        // Check Java Version (Minimum Java 17)
        String javaVersion = System.getProperty("java.version");
        String[] versionParts = javaVersion.split("\\.");
        int majorVersion;
        try {
            majorVersion = Integer.parseInt(versionParts[0]);
            if (majorVersion < 17) {
                getLogger().severe("--------------------------------------------------");
                getLogger().severe("mGiveaway Plugin Error: Incompatible Java Version");
                getLogger().severe("Your server is running Java " + javaVersion + ". ");
                getLogger().severe("mGiveaway requires Java 17 or higher to run.");
                getLogger().severe("Please update your server's Java version to 17 or later.");
                getLogger().severe("Disabling mGiveaway plugin.");
                getLogger().severe("--------------------------------------------------");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch (NumberFormatException e) {
            getLogger().warning("Could not parse Java version string: " + javaVersion + ". Java version check may be inaccurate.");
        }


        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
        } catch (ClassNotFoundException e) {
            getLogger().severe("--------------------------------------------------");
            getLogger().severe("mGiveaway Plugin Error: Paper Server Required");
            getLogger().severe("mGiveaway is designed for Paper servers (1.17+).");
            getLogger().severe("It appears your server is NOT running Paper.");
            getLogger().severe("Please use a Paper server version 1.17 or higher.");
            getLogger().severe("Functionality is NOT guaranteed on Spigot or Bukkit.");
            getLogger().severe("Disabling mGiveaway plugin.");
            getLogger().severe("--------------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return; // Stop plugin loading
        }

        getLogger().info("Loading bStats...");
        metrics = new Metrics(this, 24362);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI not found or not enabled! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(Bukkit.getPluginManager().getPlugin("Vault") == null || !Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            getLogger().severe("Vault not found or not enabled! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("PlaceholderAPI and Vault found and enabled!");

        if (!setupPermissions()) {
            getLogger().severe("Vault permissions not found or not enabled! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new Reload();

        new ConfigUtil();
        TextUtil.setInstance(this);
        TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);

        discordUtil = new DiscordUtil();

        if(isPaused())
            return;

        discordUtil.build();

        if(isPaused())
            return;

        new DiscordListener();

        dbUtils = new DBUtils();

        if(isPaused())
            return;

        getLogger().info("Plugin enabled!");
        int interval = ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60 == 0 ? 1 : ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60;
        final int[] n = {interval-1};
        String message = ConfigUtil.getAndValidate(ConfigUtil.BROADCAST_MESSAGE);
        updateGiveaways = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if(!isPaused()) {
                n[0]++;
                ConfigUtil.reloadConfig();
                TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);
                List<Giveaway> newGiveaways = fetchGiveaways();

                for (Giveaway giveaway : newGiveaways) {
                    if (!giveaways.contains(giveaway)) {
                        getLogger().info("New giveaway found: " + giveaway.getName());
                        giveaways.add(giveaway);
                    }
                }

                for (Giveaway giveaway : giveaways) {
                    if ((!giveaway.isStarted() && giveaway.getStartTime() != null && giveaway.getStartTimeFormatted().isBefore(LocalDateTime.now())) || giveaway.shouldStart()) {
                        getLogger().info("Starting giveaway: " + giveaway.getName());
                        String id = discordUtil.sendGiveawayEmbed(giveaway);
                        giveaway.setEmbedId(id);
                        giveaway.setStarted(true);
                        ConfigUtil.getConfig().set(ConfigUtil.STARTED.replace("%s", giveaway.getName()), true);
                        ConfigUtil.saveConfig();
                    }
                    if (!giveaway.hasEnded() && giveaway.isStarted() && giveaway.getEndTimeFormatted().isBefore(LocalDateTime.now())) {
                        getLogger().info("Ending giveaway: " + giveaway.getName());
                        List<String> winners = giveaway.endGiveaway();
                        for (String winner : winners) {
                            ConfigUtil.updateStat(winner, 2);
                            String nick = giveaway.getEntryMap().get(winner);
                            instance.getServer().getScheduler().runTask(instance, () -> {
                                for (String command : giveaway.getCommands()) {
                                    instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command.replace("%player%", nick));
                                }
                            });
                        }
                        discordUtil.sendGiveawayEndEmbed(giveaway, winners);
                        TextUtil.sendLogEmbed(giveaway);
                    } else if (!giveaway.hasEnded() && giveaway.isStarted() && n[0] % interval == 0) {
                        Bukkit.broadcastMessage(TextUtil.process(message
                                .replace("%winners%", String.valueOf(giveaway.getWinCount()))
                                .replace("%prize%", giveaway.getMinecraftPrize())
                                .replace("%time_left%", giveaway.getTimeLeft())));
                        n[0] = 0;
                    }
                }
            }
        }, 120, 20*60);

        updateCheck = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            instance.getLogger().info("Checking for updates...");
            new UpdateChecker(this, UpdateCheckSource.GITHUB_RELEASE_TAG, "m-surowiec/mGiveaway")
                    .setNotifyOpsOnJoin(true)
                    .setDownloadLink("https://github.com/m-surowiec/mGiveaway/releases/latest")
                    .setColoredConsoleOutput(true)
                    .checkNow();
        }, 120, 20*60*30);
    }

    @Override
    public void onDisable() {
        if (updateGiveaways != null && !updateGiveaways.isCancelled()) {
            updateGiveaways.cancel();
        }

        for(Giveaway giveaway : giveaways) {
            dbUtils.saveEntries(giveaway);
        }

        try {
            if(discordUtil == null) return;
            JDA jda = discordUtil.getJDA();
            if (jda == null) return;
            getLogger().info("Shutting down Discord bot...");
            jda.shutdown();
            if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                jda.shutdownNow();
                jda.awaitShutdown();
            }
            getLogger().info("Discord bot shut down successfully!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static MGiveaway getInstance() {
        return instance;
    }

    public DiscordUtil getDiscordUtil() {
        return discordUtil;
    }

    private List<Giveaway> fetchGiveaways() {
        ConfigurationSection section = ConfigUtil.getConfig().getConfigurationSection("giveaways");
        if (section == null) return List.of();
        if (section.getKeys(false).isEmpty()) return List.of();

        List<Giveaway> giveaways = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection giveawaySection = section.getConfigurationSection(key);
            if (giveawaySection == null) continue;
            giveaways.add(new Giveaway(instance).fromConfig(giveawaySection.getName()));
        }

        return giveaways;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        assert rsp != null;
        perms = rsp.getProvider();
        return true;
    }

    public Permission getPerms() {
        return perms;
    }


    public DBUtils getDBUtil() {
        return dbUtils;
    }

    public List<Giveaway> getGiveaways() {
        return giveaways;
    }

    public Giveaway getGiveaway(String name) {
        Giveaway giveaway = giveaways.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (giveaway != null) {
            HashMap<String, String> entry = entries.get(giveaway);
            if (entry != null) {
                giveaway.setEntryMap(entry);
            }
            return giveaway;
        }
        return null;
    }

    public static boolean isPaused() {
        return pausePlugin;
    }

    public static void setPaused(boolean paused) {
        pausePlugin = paused;
    }

    /**
     * Completely reloads the plugin.
     * This method is called when the plugin is reloaded.
     * It should be used to reload all the plugin's data.
     *
     */
    public void reloadPlugin() {
        onDisable();

        clearGiveaways();

        setPaused(false);

        getLogger().info("Reloading plugin...");

        getLogger().info("Resetting bStats...");
        metrics.shutdown();
        metrics = new Metrics(this, 24362);

        getLogger().info("Reloading config...");
        new ConfigUtil();
        TextUtil.setInstance(this);
        TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);

        getLogger().info("Reloading Discord bot...");
        if(discordUtil != null && discordUtil.getJDA() != null && discordUtil.getJDA().getStatus() == JDA.Status.CONNECTED) {
            discordUtil.getJDA().shutdown();
        }
        discordUtil = new DiscordUtil();
        discordUtil.build();
        new DiscordListener();

        dbUtils = new DBUtils();

        getLogger().info("Starting giveaway update task...");
        if (updateGiveaways != null && !updateGiveaways.isCancelled()) {
            updateGiveaways.cancel();
            updateGiveaways = null;
        }
        int interval = ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60 == 0 ? 1 : ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60;
        final int[] n = {interval-1};
        String message = ConfigUtil.getAndValidate(ConfigUtil.BROADCAST_MESSAGE);
        updateGiveaways = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if(!isPaused()) {
                n[0]++;
                ConfigUtil.reloadConfig();
                TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);
                List<Giveaway> newGiveaways = fetchGiveaways();

                for (Giveaway giveaway : newGiveaways) {
                    if (!giveaways.contains(giveaway)) {
                        getLogger().info("New giveaway found: " + giveaway.getName());
                        giveaways.add(giveaway);
                    }
                }

                for (Giveaway giveaway : giveaways) {
                    if ((!giveaway.isStarted() && giveaway.getStartTime() != null && giveaway.getStartTimeFormatted().isBefore(LocalDateTime.now())) || giveaway.shouldStart()) {
                        getLogger().info("Starting giveaway: " + giveaway.getName());
                        String id = discordUtil.sendGiveawayEmbed(giveaway);
                        giveaway.setEmbedId(id);
                        giveaway.setStarted(true);
                        ConfigUtil.getConfig().set(ConfigUtil.STARTED.replace("%s", giveaway.getName()), true);
                        ConfigUtil.saveConfig();
                    }
                    if (!giveaway.hasEnded() && giveaway.isStarted() && giveaway.getEndTimeFormatted().isBefore(LocalDateTime.now())) {
                        getLogger().info("Ending giveaway: " + giveaway.getName());
                        List<String> winners = giveaway.endGiveaway();
                        for (String winner : winners) {
                            ConfigUtil.updateStat(winner, 2);
                            String nick = giveaway.getEntryMap().get(winner);
                            instance.getServer().getScheduler().runTask(instance, () -> {
                                for (String command : giveaway.getCommands()) {
                                    instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command.replace("%player%", nick));
                                }
                            });
                        }
                        discordUtil.sendGiveawayEndEmbed(giveaway, winners);
                        TextUtil.sendLogEmbed(giveaway);
                    } else if (!giveaway.hasEnded() && giveaway.isStarted() && n[0] % interval == 0) {
                        Bukkit.broadcastMessage(TextUtil.process(message
                                .replace("%winners%", String.valueOf(giveaway.getWinCount()))
                                .replace("%prize%", giveaway.getMinecraftPrize())
                                .replace("%time_left%", giveaway.getTimeLeft())));
                        n[0] = 0;
                    }
                }
            }
        }, 120, 20*60);


        getLogger().info("Reloading plugin complete!");
    }

    public void clearGiveaways() {
        giveaways.clear();
    }
}
