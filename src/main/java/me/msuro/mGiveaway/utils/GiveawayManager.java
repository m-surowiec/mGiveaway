package me.msuro.mGiveaway.utils;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.Giveaway;
import me.msuro.mGiveaway.Requirement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class GiveawayManager {

    private final MGiveaway instance;
    // Giveaway name -> Giveaway object
    private HashMap<String, Giveaway> giveaways = new HashMap<>();

    public GiveawayManager() {
        this.instance = MGiveaway.getInstance();
    }

    public void startGiveaway(Giveaway giveaway) {
        giveaway = giveaway.withState(Giveaway.State.STARTED);
        String id = instance.getDiscordUtil().sendGiveawayEmbed(giveaway);
        giveaway = giveaway.withEmbedId(id);
        ConfigUtil.getConfig().set(ConfigUtil.STARTED.replace("%s", giveaway.name()), true);
        ConfigUtil.getConfig().set(ConfigUtil.EMBED_ID.replace("%s", giveaway.name()), id);
        ConfigUtil.saveConfig();
        giveaways.put(giveaway.name(), giveaway);
    }

    public void endGiveaway(Giveaway giveaway) {
        HashMap<String, String> winners = drawWinners(giveaway);
        giveaway = giveaway.withState(Giveaway.State.ENDED);
        giveaway = giveaway.withWinners(winners);
        instance.getDiscordUtil().sendGiveawayEndEmbed(giveaway, winners);
        TextUtil.sendLogEmbed(giveaway);
        ConfigUtil.getConfig().set(ConfigUtil.ENDED.replace("%s", giveaway.name()), true);
        ConfigUtil.saveConfig();
        putGiveaway(giveaway);
        List<String> commands = giveaway.prizeCommands();
        instance.getServer().getScheduler().runTask(instance, () -> {
            for(String value : winners.values()) {
                for(String command : commands) {
                    instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command.replace("%player%", value));
                }
            }
        });

    }

    /**
     * Adds a new giveaway to the manager. Used for adding new giveaways and editing those that already exist. (for example when a new entry is added)
     * @param giveaway The giveaway to add.
     *                 If a giveaway with the same name already exists, it will be replaced.
     *                 If the giveaway is new, it will be added.
     * @return The updated map of giveaways.
     */
    public HashMap<String, Giveaway> putGiveaway(Giveaway giveaway) {
        giveaways.put(giveaway.name(), giveaway);
        return giveaways;
    }

    public void editEntry() {
        // Edit an entry in a giveaway
    }

    public Giveaway addEntry(Giveaway giveaway, String id, String nick) {
        HashMap<String, String> entries = giveaway.entries();
        entries.put(id, nick);
        giveaway = giveaway.withEntries(entries);
        MGiveaway.getInstance().getLogger().info("[" + giveaway.name() + "] Added entry: " + id + " (" + nick + ") " + entries.size());
        putGiveaway(giveaway);
        return giveaway;
    }


    public HashMap<String, String> drawWinners(Giveaway giveaway) {
        HashMap<String, String> entries = giveaway.entries();
        int winCount = giveaway.winCount();
        HashMap<String, String> winners = new HashMap<>();
        if (entries.size() <= winCount) {
            winners.putAll(entries);
        } else {
            List<String> keys = new ArrayList<>(entries.keySet());
            Collections.shuffle(keys);
            for (int i = 0; i < winCount; i++) {
                String key = keys.get(i);
                winners.put(key, entries.get(key));
            }
        }
        giveaway = giveaway.withWinners(winners);
        putGiveaway(giveaway);
        return winners;
    }

    public HashMap<String, Giveaway> listGiveaways() {
        if(giveaways == null || giveaways.isEmpty()) {
            fetchGiveaways();
        }
        return giveaways;
    }

    public void listEntries() {
        // List all entries for a giveaway
    }

    public void listWinners() {
        // List all winners for a giveaway
    }

    public void listRequirements() {
        // List all requirements for a giveaway
    }

    public void listCommands() {
        // List all commands for a giveaway
    }

    public HashMap<String, Giveaway> fetchGiveaways() {
        giveaways.clear();
        //MGiveaway.getInstance().getLogger().info("Fetching giveaways...");
        ConfigurationSection section = instance.getConfig().getConfigurationSection("giveaways");
        if (section == null) {
            return giveaways;
        }

        for (String key : section.getKeys(false)) {
            Giveaway giveaway = giveawayFromConfig(key);
            if (giveaway != null) {
                giveaways.put(key, giveaway);
            }
        }
        return giveaways;

    }

    public Giveaway giveawayFromConfig(String name) {
        if(name == null || name.isBlank()) {
            return null;
        }
        ConfigurationSection section = instance.getConfig().getConfigurationSection("giveaways." + name);
        if (section == null || section.getKeys(true).size() < 2) {
            return null;
        }
        Giveaway giveaway = new Giveaway(
                name,
                ConfigUtil.getAndValidate(ConfigUtil.PRIZE_FORMATTED.replace("%s", name)),
                ConfigUtil.getAndValidate(ConfigUtil.MINECRAFT_PRIZE.replace("%s", name)),
                ConfigUtil.getAndValidate(ConfigUtil.END_TIME.replace("%s", name)),
                null,
                ConfigUtil.getOptional(ConfigUtil.SCH_START.replace("%s", name)),
                null,
                ConfigUtil.getInt(ConfigUtil.WINNERS.replace("%s", name)),
                ConfigUtil.getOptional(ConfigUtil.EMBED_ID.replace("%s", name)),
                ConfigUtil.getConfig().getBoolean(ConfigUtil.ENDED.replace("%s", name)) ? Giveaway.State.ENDED : ConfigUtil.getConfig().getBoolean(ConfigUtil.STARTED.replace("%s", name)) ? Giveaway.State.STARTED : Giveaway.State.NOT_STARTED,
                new HashMap<>(),
                ConfigUtil.getConfig().getStringList(ConfigUtil.COMMANDS.replace("%s", name)),
                new HashMap<>(),
                loadRequirements(section)

        );
        giveaway = giveaway.withEntries(instance.getDBUtil().refreshEntries(giveaway));
        giveaway = giveaway.withEndTime(giveaway.endTime());
        giveaway = giveaway.withStartTime(giveaway.startTime());
        return giveaway;
    }


    private List<Requirement> loadRequirements(ConfigurationSection section) {
        List<Requirement> requirements = new ArrayList<>();
        loadPermissionRequirements(requirements, section);
        loadGroupRequirements(requirements, section);
        loadPlaceholderRequirements(requirements, section);
        return requirements;
    }

    private void loadPermissionRequirements(List<Requirement> requirements, ConfigurationSection section) {
        ConfigurationSection permSection = section.getConfigurationSection(section.getCurrentPath() + ".requirements.permission");
        if (permSection != null) {
            for (String key : permSection.getKeys(false)) {
                try{
                    Requirement req = new Requirement(
                            key,
                            Requirement.Type.PERMISSION,
                            permSection.getBoolean(key + ".value"),
                            -2147483648,
                            ConfigUtil.getOptional(permSection.getCurrentPath() + "." + key + ".formatted"));
                    requirements.add(req);
                }
                catch (Exception e){
                    MGiveaway.getInstance().getLogger().severe("Error loading permission requirement " + key + " : " + e.getMessage());
                }
            }
        }
    }

    private void loadGroupRequirements(List<Requirement> requirements, ConfigurationSection section) {
        ConfigurationSection groupSection = section.getConfigurationSection(section.getCurrentPath() + ".requirements.group");
        if (groupSection != null) {
            for (String key : groupSection.getKeys(false)) {
                try{
                    Requirement req = new Requirement(
                            key,
                            Requirement.Type.ROLE,
                            groupSection.getBoolean(key + ".value"),
                            -2147483648,
                            ConfigUtil.getOptional(groupSection.getCurrentPath() + "." + key + ".formatted"));
                    requirements.add(req);
                }
                catch (Exception e){
                    MGiveaway.getInstance().getLogger().severe("Error loading group requirement " + key + " : " + e.getMessage());
                }
            }
        }
    }
    private void loadPlaceholderRequirements(List<Requirement> requirements, ConfigurationSection section) {
        ConfigurationSection placeholderSection = section.getConfigurationSection(section.getCurrentPath() + ".requirements.placeholder");
        if (placeholderSection != null) {
            for (String key : placeholderSection.getKeys(false)) {
                try{
                    String valueOver = ConfigUtil.getOptional(placeholderSection.getCurrentPath() + "." + key + ".over");
                    String valueUnder = ConfigUtil.getOptional(placeholderSection.getCurrentPath() + "." + key + ".under");
                    if (valueOver != null) {
                        Requirement req = new Requirement(
                                key,
                                Requirement.Type.NUMBER,
                                true, // hasToBe = true for "over"
                                Integer.parseInt(valueOver),
                                ConfigUtil.getOptional(placeholderSection.getCurrentPath() + "." + key + ".formatted"));
                        requirements.add(req);
                    }
                    if (valueUnder != null) {
                        Requirement req = new Requirement(
                                key,
                                Requirement.Type.NUMBER,
                                false, // hasToBe = false for "under"
                                Integer.parseInt(valueUnder),
                                ConfigUtil.getOptional(placeholderSection.getCurrentPath() + "." + key + ".formatted"));
                        requirements.add(req);
                    }
                }
                catch (NumberFormatException e){
                    MGiveaway.getInstance().getLogger().severe("Error loading placeholder requirement " + key + " : Invalid number " + e.getMessage());
                }
                catch (Exception e){
                    MGiveaway.getInstance().getLogger().severe("Error loading placeholder requirement " + key + " : " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if the player meets the requirements for this giveaway asynchronously.
     *
     * @param username The player's username.
     * @param callback A Consumer that accepts a List<Requirement>. This callback will be
     *                 executed asynchronously on the main thread once the requirements
     *                 check is complete. The list will contain unmet requirements, or an
     *                 empty list if all are met, or null if player is not found after API lookup.
     */
    public void checkRequirementsAsync(Giveaway giveaway, String username, Consumer<List<Requirement>> callback) {
        if (giveaway.requirements().isEmpty()) {
            callback.accept(new ArrayList<>()); // No requirements, return empty list immediately
            return;
        }

        OfflinePlayer player = null;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            player = MGiveaway.getInstance().getServer().getOfflinePlayerIfCached(username);
        } catch (ClassNotFoundException e) {
            player = Bukkit.getOfflinePlayer(username);
        }

        if (player != null) {
            // Player is cached, perform synchronous checks and callback immediately on main thread
            List<Requirement> notMet = checkRequirementsSync(giveaway, player); // Helper method for sync checks
            callback.accept(notMet);
        } else {
            // Player not cached, perform asynchronous API lookup
            new BukkitRunnable() {
                @Override
                public void run() {
                    OfflinePlayer asyncPlayer = Bukkit.getOfflinePlayer(username);
                    List<Requirement> notMet;

                    if (!asyncPlayer.hasPlayedBefore()) {
                        notMet = List.of(new Requirement("Player not found", Requirement.Type.NULLPLAYER, false, -2147483648, "Player not found")); // Return NULLPLAYER requirement
                    } else {
                        notMet = checkRequirementsSync(giveaway, asyncPlayer); // Perform sync checks with API-fetched player
                    }

                    // Execute callback on the main thread with the result
                    Bukkit.getScheduler().runTask(MGiveaway.getInstance(), () -> {
                        callback.accept(notMet);
                    });
                }
            }.runTaskAsynchronously(MGiveaway.getInstance());
        }
    }

    /**
     * Helper method to perform synchronous requirement checks given an OfflinePlayer object.
     * This is reused for both cached and API-fetched players to avoid code duplication.
     *
     * @param player The OfflinePlayer to check requirements against.
     * @return A List of unmet Requirements.
     */
    private List<Requirement> checkRequirementsSync(Giveaway giveaway, OfflinePlayer player) {
        List<Requirement> notMet = new ArrayList<>();
        for (Requirement requirement : giveaway.requirements()) {
            if (!requirement.check(player)) {
                notMet.add(requirement);
            }
        }
        return notMet;
    }

    public void clearGiveaways() {
        giveaways.clear();
    }
}