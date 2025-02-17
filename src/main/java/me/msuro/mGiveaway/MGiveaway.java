package me.msuro.mGiveaway;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.commands.Reload;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DBUtils;
import me.msuro.mGiveaway.utils.DiscordUtil;
import me.msuro.mGiveaway.utils.GiveawayManager;
import me.msuro.mGiveaway.utils.TextUtil;
import net.dv8tion.jda.api.JDA;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;

public final class MGiveaway extends JavaPlugin {

    private static MGiveaway instance;
    private DiscordUtil discordUtil;
    private DBUtils dbUtils;
    private GiveawayManager giveawayManager; // Use GiveawayManager
    private static boolean pausePlugin = false;
    private Metrics metrics;
    private BukkitTask updateGiveaways;
    private BukkitTask updateCheck;
    private Permission perms = null;

    @Override
    public void onEnable() {
        instance = this;
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
            return;
        }

        getLogger().info("Loading bStats...");
        metrics = new Metrics(this, 24362);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI not found or not enabled! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null || !Bukkit.getPluginManager().isPluginEnabled("Vault")) {
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

        // --- Initialize GiveawayManager and other components ---
        giveawayManager = new GiveawayManager();
        new PlayerListener();
        new UpdateListener();
        new Reload();
        new ConfigUtil();
        TextUtil.setInstance(this);
        TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);
        discordUtil = new DiscordUtil();
        dbUtils = new DBUtils();

        if (isPaused())
            return;

        discordUtil.build();

        if (isPaused())
            return;

        new DiscordListener();

        if (isPaused())
            return;


        getLogger().info("Plugin enabled!");

        // --- Asynchronous Tasks (updateGiveaways and updateCheck) ---
        resetUpdateGiveaways();
        new UpdateChecker(this, UpdateCheckSource.SPIGET, "122302")
                .setDownloadLink("https://www.spigotmc.org/resources/mgiveaway.122302/")
                .setNotifyRequesters(false)
                .checkNow()
                .checkEveryXHours(1);
    }


    @Override
    public void onDisable() {
        if (updateGiveaways != null && !updateGiveaways.isCancelled()) {
            updateGiveaways.cancel();
        }

        // Save entries for all giveaways (using GiveawayManager)
        if(giveawayManager != null){
            for (Giveaway giveaway : giveawayManager.listGiveaways().values()) {
                dbUtils.saveEntries(giveaway);
            }
        }

        try {
            if (discordUtil == null) return;
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

    // --- Access Giveaways through GiveawayManager ---
    public Giveaway getGiveaway(String name) {
        return giveawayManager.listGiveaways().get(name); // Use GiveawayManager
    }

    public static boolean isPaused() {
        return pausePlugin;
    }

    public static void setPaused(boolean paused) {
        pausePlugin = paused;
    }
    public void reloadPlugin() {
        onDisable();

        for (Giveaway giveaway : giveawayManager.listGiveaways().values()) {
            dbUtils.saveEntries(giveaway);
        }
        giveawayManager.clearGiveaways();

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
        if (discordUtil != null && discordUtil.getJDA() != null && discordUtil.getJDA().getStatus() == JDA.Status.CONNECTED) {
            discordUtil.getJDA().shutdown();
        }
        discordUtil = new DiscordUtil();
        discordUtil.build();
        new DiscordListener();
        dbUtils = new DBUtils();
        giveawayManager.fetchGiveaways();
        resetUpdateGiveaways();

        getLogger().info("Reloading plugin complete!");
    }

    public void clearGiveaways() {
        giveawayManager.clearGiveaways();
    }

    public GiveawayManager getGiveawayManager() {
        return giveawayManager;
    }

    private BukkitTask resetUpdateGiveaways() {
        if (updateGiveaways != null && !updateGiveaways.isCancelled()) {
            updateGiveaways.cancel();
        }
        int interval = ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL) / 60 == 0 ? 1 : ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL) / 60;
        final int[] n = {interval - 1};
        updateGiveaways = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (!isPaused()) {
                String message = ConfigUtil.getAndValidate(ConfigUtil.BROADCAST_MESSAGE);
                n[0]++;
                ConfigUtil.reloadConfig();
                TextUtil.prefix = ConfigUtil.getOrDefault(ConfigUtil.PREFIX);
                HashMap<String, Giveaway> newGiveaways = giveawayManager.fetchGiveaways();

                for (String name : newGiveaways.keySet()) {
                    if (giveawayManager.listGiveaways().get(name) == null) {
                        getLogger().info("New giveaway found: " + newGiveaways.get(name).name());
                        giveawayManager.putGiveaway(newGiveaways.get(name));
                    }
                }

                for (Giveaway giveaway : giveawayManager.listGiveaways().values()) {
                    if (giveaway.shouldStart()) {
                        giveawayManager.startGiveaway(giveaway);
                    }
                    if (giveaway.shouldEnd()) {
                        giveawayManager.endGiveaway(giveaway);
                    } else if (!giveaway.state().equals(Giveaway.State.ENDED)
                            && giveaway.state().equals(Giveaway.State.STARTED)
                            && n[0] % interval == 0) {
                        Bukkit.broadcastMessage(TextUtil.process(message
                                .replace("%winners%", String.valueOf(giveaway.winCount()))
                                .replace("%prize%", giveaway.minecraftPrize())
                                .replace("%time_left%", giveaway.getTimeLeft())));
                        n[0] = 0;
                    }
                }
            }
        }, 120, 20 * 60);
        return updateGiveaways;
    }

}