package me.msuro.mGiveaway;

import me.msuro.mGiveaway.classes.Giveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.DiscordUtil;
import net.dv8tion.jda.api.JDA;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class MGiveaway extends JavaPlugin {

    private static MGiveaway instance;
    private DiscordUtil discordUtil;

    private BukkitTask updateGiveaways;

    private List<Giveaway> giveaways = new ArrayList<>();



    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getLogger().info("Enabling plugin...");

        new ConfigUtil();

        discordUtil = new DiscordUtil();
        discordUtil.build();
        new DiscordListener();

        getLogger().info("Plugin enabled!");

        updateGiveaways = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            ConfigUtil.reloadConfig();
            getLogger().info("Updating giveaways...");
            List<Giveaway> oldGiveaways = giveaways;
            giveaways = fetchGiveaways();

            for(Giveaway giveaway : giveaways) {
                //getLogger().info("Checking giveaway: " + giveaway.toString());
                if(!oldGiveaways.contains(giveaway)) {
                    getLogger().info("New giveaway found: " + giveaway.getName());
                }
                if(!giveaway.isStarted() && giveaway.getStartTime() != null && giveaway.getStartTimeFormatted().isBefore(LocalDateTime.now())) {
                    getLogger().info("Starting giveaway: " + giveaway.getName());
                    String id = discordUtil.sendGiveawayEmbed(giveaway);
                    giveaway.setEmbedId(id);
                    giveaway.setStarted(true);
                    ConfigUtil.getConfig().set(ConfigUtil.STARTED.replace("%s", giveaway.getName()), true);
                    ConfigUtil.saveConfig();
                }
                if(giveaway.isStarted() && giveaway.getEndTimeFormatted().isBefore(LocalDateTime.now())) {
                    getLogger().info("Ending giveaway: " + giveaway.getName());
                    List<String> winners = giveaway.endGiveaway();
                    for (String winner : winners) {
                        ConfigUtil.updateStat(winner, 2);
                        String nick = ConfigUtil.getAndValidate(ConfigUtil.ENTRIES.replace("%s", giveaway.getName() + "." + winner));
                        BukkitTask syncTask = instance.getServer().getScheduler().runTask(instance, () -> {
                            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), giveaway.getCommand().replace("%player%", nick));
                        });
                    }
                    discordUtil.sendGiveawayEndEmbed(giveaway, winners);
                }
            }
            // todo - restore the interval to a minute (20*60)
        }, 120, 20*10);
    }

    @Override
    public void onDisable() {
        try {
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

        if (updateGiveaways != null && !updateGiveaways.isCancelled()) {
            updateGiveaways.cancel();
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
            giveaways.add(new Giveaway().fromConfig(giveawaySection.getName()));
        }

        return giveaways;
    }

}
