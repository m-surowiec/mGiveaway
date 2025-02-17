package me.msuro.mGiveaway.classes;

import me.msuro.mGiveaway.utils.ConfigUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public record Giveaway(
        String                  name,            // REQUIRED - The unique internal name of the giveaway.
        String                  prize,           // REQUIRED - The formatted prize description (for Discord embeds).
        String                  minecraftPrize,  // REQUIRED - The short, plain-text prize description (for in-game broadcasts).
        String                  endTime,         // REQUIRED - The end date/time string (in "dd/MM/yyyy HH:mm:ss" format).
        LocalDateTime           endTimeParsed,   // REQUIRED - The parsed end date/time.
        String                  startTime,       // OPTIONAL - The scheduled start date/time string (in "dd/MM/yyyy HH:mm:ss" format).  Null if starts immediately.
        LocalDateTime           startTimeParsed, // OPTIONAL - The parsed start date/time. Null if starts immediately.
        Integer                 winCount,        // REQUIRED - The number of winners.
        String                  embedId,         // OPTIONAL - The Discord message ID of the giveaway embed. Null initially.
        State                   state,           // REQUIRED - The current state of the giveaway (PENDING, STARTED, ENDED).
        HashMap<String, String> entries,         // REQUIRED - A map of Discord user IDs to Minecraft usernames (entries).
        List<String>            prizeCommands,   // REQUIRED - A list of RewardCommand objects (command + execution mode).
        HashMap<String, String> winners,         // OPTIONAL - A list of winner Discord user IDs.  Empty initially.
        List<Requirement>       requirements     // REQUIRED - A list of entry requirements.
        ) {

        public enum State {
        NOT_STARTED,
        STARTED,
        ENDED
    }

    // Methods for filling optional fields

    public Giveaway withStartTime(String startTime) {
        LocalDateTime startTimeParsed = startTime != null ? LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null;
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withEmbedId(String embedId) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withWinners(HashMap<String, String> winners) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    // Methods for updating the rest of the fields

    public Giveaway withName(String name) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withPrize(String prize) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withMinecraftPrize(String minecraftPrize) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withEndTime(String endTime) {
        LocalDateTime endTimeParsed = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withWinCount(Integer winCount) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withState(State state) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withEntries(HashMap<String, String> entries) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withPrizeCommands(List<String> prizeCommands) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    public Giveaway withRequirements(List<Requirement> requirements) {
        return new Giveaway(name, prize, minecraftPrize, endTime, endTimeParsed, startTime, startTimeParsed, winCount, embedId, state, entries, prizeCommands, winners, requirements);
    }

    // Helper methods

    /**
     * Returns whether the giveaway should start immediately.
     * This is determined by the state, scheduled start time and the presence of a force-start flag. (From the discord command)
     * @return True if the giveaway should start immediately, false otherwise.
     */
    public boolean shouldStart() {
        return state == State.NOT_STARTED && (startTimeParsed == null || (startTimeParsed().isBefore(LocalDateTime.now()) && ConfigUtil.getOptional(ConfigUtil.FORCE_START.replace("%s", name)) != null));
    }

    /**
     * Returns whether the giveaway should end.
     * This is determined by the state and the end time.
     * @return True if the giveaway should end, false otherwise.
     */
    public boolean shouldEnd() {
        return state == State.STARTED && endTimeParsed.isBefore(LocalDateTime.now());
    }

    public boolean hasEnded() {
        return state == State.ENDED;
    }

    /**
     * Returns the time left until the end of the giveaway in "xd yh zm" format.
     *
     * @return The formatted time left until the end of the giveaway.
     */
    public String getTimeLeft() {
        LocalDateTime now = LocalDateTime.now();
        long diff = endTimeParsed.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC);
        if (diff <= 0) {
            return "0m";
        }
        long days = diff / 86400;
        diff -= days * 86400;
        long hours = diff / 3600;
        diff -= hours * 3600;
        long minutes = diff / 60;
        return days + "d " + hours + "h " + minutes + "m";
    }

    public void addEntry(String id, String nick) {
        entries.put(id, nick);
    }

    @Override
    public String toString() {
        return "Giveaway{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", prize='" + prize + '\'' +
                ", winners=" + (winners == null ? "[]" : winners.size()) +  // Show winner *count*
                ", entries=" + (entries == null ? "{}" : entries.size()) +  // Show entry *count*
                ", endTime=" + (endTimeParsed == null ? "null" : endTimeParsed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) +
                (startTimeParsed != null ? ", startTime=" + startTimeParsed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "") + // Only if exists
                ", embedId='" + (embedId == null ? "null" : embedId) + '\'' +
                ", commands=" + (prizeCommands == null ? "[]" : prizeCommands.size() )+
                ", requirements=" + (requirements == null ? "[]" : requirements.size()) +
                '}';
    }
    
}