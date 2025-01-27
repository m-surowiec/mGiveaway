package me.msuro.mGiveaway;

import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DBUtils;
import me.msuro.mGiveaway.utils.DiscordUtil;
import me.msuro.mGiveaway.utils.TextUtil;
import me.msuro.mGiveaway.utils.colors.ColorAPI;
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

    private BukkitTask updateGiveaways;

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

        getLogger().info("Loading bStats...");
        new Metrics(this, 24362);

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

        new ConfigUtil();
        TextUtil.setInstance(this);
        TextUtil.prefix = ConfigUtil.getAndValidate(ConfigUtil.PREFIX);

        discordUtil = new DiscordUtil();
        discordUtil.build();
        new DiscordListener();

        dbUtils = new DBUtils();

        getLogger().info("Plugin enabled!");
        int interval = ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60 == 0 ? 1 : ConfigUtil.getInt(ConfigUtil.BROADCAST_INTERVAL)/60;
        final int[] n = {interval-1};
        String message = ConfigUtil.getAndValidate(ConfigUtil.BROADCAST_MESSAGE);
        updateGiveaways = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            n[0]++;
            ConfigUtil.reloadConfig();
            TextUtil.prefix = ConfigUtil.getAndValidate(ConfigUtil.PREFIX);
            List<Giveaway> newGiveaways = fetchGiveaways();

            for(Giveaway giveaway : newGiveaways) {
                if(!giveaways.contains(giveaway)) {
                    getLogger().info("New giveaway found: " + giveaway.getName());
                    giveaways.add(giveaway);
                }
             }

            for(Giveaway giveaway : giveaways) {
                if((!giveaway.isStarted() && giveaway.getStartTime() != null && giveaway.getStartTimeFormatted().isBefore(LocalDateTime.now())) || giveaway.shouldStart()) {
                    getLogger().info("Starting giveaway: " + giveaway.getName());
                    String id = discordUtil.sendGiveawayEmbed(giveaway);
                    giveaway.setEmbedId(id);
                    giveaway.setStarted(true);
                    ConfigUtil.getConfig().set(ConfigUtil.STARTED.replace("%s", giveaway.getName()), true);
                    ConfigUtil.saveConfig();
                }
                if(!giveaway.hasEnded() && giveaway.isStarted() && giveaway.getEndTimeFormatted().isBefore(LocalDateTime.now())) {
                    getLogger().info("Ending giveaway: " + giveaway.getName());
                    List<String> winners = giveaway.endGiveaway();
                    for (String winner : winners) {
                        ConfigUtil.updateStat(winner, 2);
                        String nick = giveaway.getEntryMap().get(winner);
                        instance.getServer().getScheduler().runTask(instance, () -> {
                            for(String command : giveaway.getCommands()) {
                                instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command.replace("%player%", nick));
                            }
                        });
                    }
                    discordUtil.sendGiveawayEndEmbed(giveaway, winners);
                    TextUtil.sendGiveawayEmbed(giveaway);
                } else if(!giveaway.hasEnded() && giveaway.isStarted() && n[0] % interval == 0) {
                    Bukkit.broadcastMessage(TextUtil.process(message
                            .replace("%winners%", String.valueOf(giveaway.getWinCount()))
                            .replace("%prize%", giveaway.getPrizePlaceholder())
                            .replace("%time_left%", giveaway.getTimeLeft())));
                    n[0] = 0;
                }
            }
        }, 120, 20*60);
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
        perms = rsp.getProvider();
        return perms != null;
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
        }
        return giveaways.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
