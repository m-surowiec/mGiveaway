package me.msuro.mGiveaway.classes;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Giveaway {

    // Giveaway settings
    private String name = "null";
    private String prize = "null";
    private String endTime = "null";
    private LocalDateTime endTimeFormatted = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    private String startTime;
    private LocalDateTime startTimeFormatted;
    private List<String> commands = new ArrayList<>();
    private Integer winCount = -1;
    private boolean started = false;

    private boolean ended = false;
    private List<String> winners = new ArrayList<>();

    private List<Requirement> requirements = new ArrayList<>();

    String embedId = "null";

    private List<String> entries = new ArrayList<>();
    private HashMap<String, String> entryMap = new HashMap<>();

    public Giveaway() {
    }

    public Giveaway fromConfig(String giveawayName) {
        this.name = giveawayName;
        // the path to giveaway is giveaways.<giveawayName>. ... so we need to replace %s with giveawayName
        this.commands = ConfigUtil.getConfig().getStringList(ConfigUtil.COMMANDS.replace("%s", giveawayName));
        this.winCount = ConfigUtil.getInt(ConfigUtil.WINNERS.replace("%s", giveawayName));
        this.started = ConfigUtil.getConfig().getBoolean(ConfigUtil.STARTED.replace("%s", giveawayName), false);
        this.endTime = ConfigUtil.getAndValidate(ConfigUtil.END_TIME.replace("%s", giveawayName));
        this.startTime = ConfigUtil.getOptional(ConfigUtil.SCH_START.replace("%s", giveawayName));
        this.prize = ConfigUtil.getAndValidate(ConfigUtil.PRIZE_FORMATTED.replace("%s", giveawayName));
        this.embedId = ConfigUtil.getOptional(ConfigUtil.EMBED_ID.replace("%s", giveawayName));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.endTimeFormatted = LocalDateTime.parse(endTime, formatter);

        if(startTime != null) {
            this.startTimeFormatted = LocalDateTime.parse(startTime, formatter);
        }

        this.entries = getEntries();
        this.requirements = getRequirements();

        return this;
    }

    public String getName() {
        return name;
    }

    public String getPrize() {
        return prize;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isEnded() {
        return endTimeFormatted.isBefore(LocalDateTime.now());
    }

    public LocalDateTime getEndTimeFormatted() {
        return endTimeFormatted;
    }

    public String getStartTime() {
        return startTime;
    }

    public LocalDateTime getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Integer getWinCount() {
        return winCount;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getEmbedId() {
        return embedId;
    }

    public void setEmbedId(String embedId) {
        this.embedId = embedId;
        ConfigUtil.getConfig().set(ConfigUtil.EMBED_ID.replace("%s", name), embedId);
        ConfigUtil.saveConfig();
    }

    /**
     * Returns a list of entries for this giveaway.
     * If the list is empty, it will refresh the entries.
     * @return A list of entries for this giveaway.
     */
    public List<String> getEntries() {
        if(entries == null || entries.isEmpty())
            refreshEntries();
        return entries;
    }

    /**
     * Returns a list of nicknames for this giveaway.
     * @return A list of nicknames for this giveaway.
     */
    public List<String> getNickEntries() {
        List<String> nicks = new ArrayList<>();
        for(String entry : getEntries()) {
            String path = (ConfigUtil.ENTRIES + "." + entry).replace("%s", name + ".");
            nicks.add(ConfigUtil.getAndValidate(path));
        }
        return nicks;
    }

    public void refreshEntries() {
        ConfigurationSection section = ConfigUtil.getConfig().getConfigurationSection(ConfigUtil.ENTRIES.replace("%s", name));
        if (section == null) {
            entries = List.of();
            return;
        }
        entries = Objects.requireNonNull(ConfigUtil.getConfig().getConfigurationSection(ConfigUtil.ENTRIES.replace("%s", name))).getKeys(false).stream().toList();
        for (String entry : entries) {
            entryMap.put(entry, ConfigUtil.getAndValidate(ConfigUtil.ENTRIES.replace("%s", name + "." + entry)));
        }
    }

    /**
     * Ends the giveaway and selects the winners.
     * @return A list of winners.
     */
    public List<String> endGiveaway() {
        List<String> winners = new ArrayList<>();
        List<String> entries = new ArrayList<>(getEntries());
        for(int i = 0; i < winCount; i++) {
            entries = getRandomEntry(entries);
            if(entries == null) {
                break;
            }
            String winner = entries.get(0);
            entries.remove(0);
            if (winner == null) {
                break;
            }
            winners.add(winner);
        }
        this.winners = winners;
        this.ended = true;
        return winners;
    }

    public List<String> getWinners() {
        return winners;
    }

    private List<String> getRandomEntry(List<String> entries) {
        if(entries.isEmpty()) {
            return null;
        }
        String winner = entries.get((int) (Math.random() * entries.size()));
        entries.remove(winner);
        entries.addLast(winner);
        return entries;
    }

    public List<Requirement> getRequirements() {
        if(requirements == null || requirements.isEmpty())
            refreshRequirements();
        return requirements;

    }

    private void refreshRequirements() {
        List<Requirement> requirements = new ArrayList<>();
        ConfigurationSection section = ConfigUtil.getConfig().getConfigurationSection(ConfigUtil.REQUIREMENT_PERMISSION.replace("%s", name));
        if(section != null)
            for(String key : section.getKeys(false)) {
                requirements.add(new Requirement(
                        key.replace("-", "."),
                        Requirement.Type.PERMISSION,
                        section.getBoolean(key + ".value"),
                        -2147483648,
                        ConfigUtil.getOptional(ConfigUtil.REQUIREMENT_FORMATTED
                                .replace("%s", name)
                                .replace("%t", "permission")
                                .replace("%r", key))));
            }
        section = ConfigUtil.getConfig().getConfigurationSection(ConfigUtil.REQUIREMENT_GROUP.replace("%s", name));
        if(section != null)
            for(String key : section.getKeys(false)) {
                requirements.add(new Requirement(
                        key,
                        Requirement.Type.ROLE,
                        section.getBoolean(key + ".value"),
                        -2147483648,
                        ConfigUtil.getOptional(ConfigUtil.REQUIREMENT_FORMATTED
                                .replace("%s", name)
                                .replace("%t", "group")
                                .replace("%r", key))));
            }
        section = ConfigUtil.getConfig().getConfigurationSection(ConfigUtil.REQUIREMENT_PLACEHOLDER.replace("%s", name));
        if(section != null)
            for (String key : section.getKeys(false)) {
                String value = ConfigUtil.getOptional(section.getCurrentPath() + "." + key + ".over");
                if (value != null)
                    requirements.add(new Requirement(
                            key,
                            Requirement.Type.NUMBER,
                            true,
                            Integer.parseInt(value),
                            ConfigUtil.getOptional(ConfigUtil.REQUIREMENT_FORMATTED
                                    .replace("%s", name)
                                    .replace("%t", "number")
                                    .replace("%r", key))));
                value = ConfigUtil.getOptional(section.getCurrentPath() + "." + key + ".under");
                if (value != null)
                    requirements.add(new Requirement(
                            key,
                            Requirement.Type.NUMBER,
                            false,
                            Integer.parseInt(value),
                            ConfigUtil.getOptional(ConfigUtil.REQUIREMENT_FORMATTED
                                    .replace("%s", name)
                                    .replace("%t", "number")
                                    .replace("%r", key))));
            }
        this.requirements = requirements;


    }

    public HashMap<String, String> getEntryMap() {
        return entryMap;
    }

    public String toString() {
        return "Giveaway{" +
                "name='" + (name != null ? name : "null") + '\'' +
                ", endTime='" + (endTime != null ? endTime : "null") + '\'' +
                ", startTime='" + (startTime != null ? startTime : "null") + '\'' +
                ", command='" + (commands != null && !commands.isEmpty() ? String.join(", ", commands) : "null") + '\'' +
                ", winCount=" + winCount +
                ", started=" + started +
                ", entries=" + entries +
                ", prize='" + prize +
                ", embedId='" + (embedId != null ? embedId : "null") + '\'' +
                ", requirements=" + requirements +
                "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Giveaway giveaway = (Giveaway) obj;
        return name.equals(giveaway.name) && endTime.equals(giveaway.endTime) && startTime.equals(giveaway.startTime);
    }

    /**
     * Checks if the player meets the requirements for this giveaway.
     * @param username The player's username.
     * @return A list of requirements that are not met.
     */
    public List<Requirement> checkRequirements(String username) {
        OfflinePlayer player = MGiveaway.getInstance().getServer().getOfflinePlayer(username);
        if(player == null) {
            throw new IllegalArgumentException("Player not found");
        }
        List<Requirement> notMet = new ArrayList<>();
        for(Requirement requirement : requirements) {
            if(!requirement.check(player)) {
                notMet.add(requirement);
            }
        }
        return notMet;

    }


    public boolean shouldStart() {
        return !started && ConfigUtil.getOptional(ConfigUtil.FORCE_START.replace("%s", name)) != null;
    }
}
