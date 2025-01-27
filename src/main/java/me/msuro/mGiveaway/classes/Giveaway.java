package me.msuro.mGiveaway.classes;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Giveaway {

    private final MGiveaway instance;
    private final String DEFAULT_VALUE = "null";

    // Giveaway settings
    private String          name                = DEFAULT_VALUE;
    private String          prize               = DEFAULT_VALUE;
    private String          prizePlaceholder    = DEFAULT_VALUE;
    private String          endTime             = DEFAULT_VALUE;
    private LocalDateTime   endTimeFormatted    = null;
    private String          startTime           = null;
    private LocalDateTime   startTimeFormatted  = null;
    private List<String>    commands            = new ArrayList<>();
    private Integer         winCount            = -1;
    private boolean         started             = false;

    private boolean ended = false;
    private List<String> winners = new ArrayList<>();

    private List<Requirement> requirements = new ArrayList<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private String embedId = DEFAULT_VALUE;

    private HashMap<String, String> entryMap = new HashMap<>();

    public Giveaway(MGiveaway instance) {
        this.instance = instance;
    }

    @Nullable
    public Giveaway fromConfig(String giveawayName) {
        if(giveawayName == null)
            throw new IllegalArgumentException("Giveaway name cannot be null");
        if(ConfigUtil.getConfig().getConfigurationSection("giveaways." + giveawayName) == null || ConfigUtil.getConfig().getConfigurationSection("giveaways." + giveawayName).getKeys(true).isEmpty())
            return null;
        this.name = giveawayName;
        // the path to giveaway is giveaways.<giveawayName>. ... so we need to replace %s with giveawayName
        this.commands = ConfigUtil.getConfig().getStringList(ConfigUtil.COMMANDS.replace("%s", giveawayName));
        this.winCount = ConfigUtil.getInt(ConfigUtil.WINNERS.replace("%s", giveawayName));
        this.started = ConfigUtil.getConfig().getBoolean(ConfigUtil.STARTED.replace("%s", giveawayName), false);
        this.endTime = ConfigUtil.getAndValidate(ConfigUtil.END_TIME.replace("%s", giveawayName));
        this.startTime = ConfigUtil.getOptional(ConfigUtil.SCH_START.replace("%s", giveawayName));
        this.prize = ConfigUtil.getAndValidate(ConfigUtil.PRIZE_FORMATTED.replace("%s", giveawayName));
        this.prizePlaceholder = ConfigUtil.getAndValidate(ConfigUtil.PRIZE_PLACEHOLDER.replace("%s", giveawayName));
        this.embedId = ConfigUtil.getOptional(ConfigUtil.EMBED_ID.replace("%s", giveawayName));


        this.endTimeFormatted = LocalDateTime.parse(endTime, formatter);

        if(startTime != null) {
            this.startTimeFormatted = LocalDateTime.parse(startTime, formatter);
        }

        this.requirements = getRequirements();

        if(name == null || endTime == null || prize == null || winCount < 0 || commands == null || prizePlaceholder == null) {
            throw new IllegalArgumentException("Giveaway settings cannot be null " + this.toString());
        }
        instance.getDBUtil().createGiveawayTable(name);
        this.entryMap = instance.getDBUtil().refreshEntries(this);
        instance.addEntry(this, entryMap);

        return this;
    }

    /**
     * Ends the giveaway and selects the winners.
     * <p>
     * This method shuffles the list of entries and selects a subset of entries as winners.
     * It then updates the giveaway status to ended in the configuration and saves the configuration.
     *
     * @return A list of winners.
     */
    public List<String> endGiveaway() {
        List<String> winners = new ArrayList<>();
        List<String> entries = new ArrayList<>(getEntryMap().keySet());
        Collections.shuffle(entries);
        for (int i = 0; i < winCount; i++) {
            if (i >= entries.size()) {
                break;
            }
            winners.add(entries.get(i));
        }
        this.winners = winners;
        ConfigUtil.getConfig().set(ConfigUtil.ENDED.replace("%s", name), true);
        ConfigUtil.saveConfig();
        return winners;
    }

    /**
     * Checks if the player meets the requirements for this giveaway.
     * @param username The player's username.
     * @return A list of requirements that are not met.
     */
    public List<Requirement> checkRequirements(String username) {

        OfflinePlayer player = MGiveaway.getInstance().getServer().getOfflinePlayerIfCached(username);
        if(player == null) {
            instance.getLogger().severe("Player " + username + " not found! Tried to join giveaway " + name);
            return List.of(new Requirement[]{new Requirement("Player not found", Requirement.Type.NULLPLAYER, false, -2147483648, "Player not found")});
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

    public boolean hasEnded() {
        String val = ConfigUtil.getOptional(ConfigUtil.ENDED.replace("%s", name));
        if(val == null) return false;
        return Boolean.parseBoolean(val);
    }

    public String getTimeLeft() {
        // return endtime-now in "xd yh zm" format
        LocalDateTime now = LocalDateTime.now();
        long diff = endTimeFormatted.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC);
        if(diff <= 0) {
            return "0m";
        }
        long days = diff / 86400;
        diff -= days * 86400;
        long hours = diff / 3600;
        diff -= hours * 3600;
        long minutes = diff / 60;
        return days + "d " + hours + "h " + minutes + "m";

    }


    public List<Requirement> getRequirements() {
        if(requirements == null || requirements.isEmpty())
            refreshRequirements();
        return requirements;

    }

    public List<String> getWinners() {
        return winners;
    }

    public String getName() {
        return name;
    }

    public String getPrize() {
        return prize;
    }

    public String getPrizePlaceholder() {
        return prizePlaceholder;
    }

    public String getEndTime() {
        return endTime;
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

    public String getEmbedId() {
        return embedId;
    }

    public HashMap<String, String> getEntryMap() {
        entryMap = instance.getEntries().get(this);
        return entryMap;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setEmbedId(String embedId) {
        this.embedId = embedId;
        ConfigUtil.getConfig().set(ConfigUtil.EMBED_ID.replace("%s", name), embedId);
        ConfigUtil.saveConfig();
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

    @Override
    public String toString() {
        return "Giveaway{" +
                "name='" + (name != null ? name : "null") + '\'' +
                ", endTime='" + (endTime != null ? endTime : "null") + '\'' +
                ", startTime='" + (startTime != null ? startTime : "null") + '\'' +
                ", command='" + (commands != null && !commands.isEmpty() ? String.join(", ", commands) : "null") + '\'' +
                ", winCount=" + winCount +
                ", started=" + started +
                ", entries=" + entryMap +
                ", prize='" + prize +
                ", prizePlaceholder='" + prizePlaceholder +
                ", winners=" + (winners != null && !winners.isEmpty() ? String.join(", ", winners) : "null") + '\'' +
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
        return name.equals(giveaway.name) && endTime.equals(giveaway.endTime) && (startTime == null || startTime.equals(giveaway.startTime));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name + endTime + startTime);
    }

    public void addEntry(String id, String nick) {
        entryMap.put(id, nick);
        instance.addEntry(this, new HashMap<>(){{put(id, nick);}});
    }

    public void setEntryMap(HashMap<String, String> entryMap) {
        this.entryMap = entryMap;
    }
}