package me.msuro.mGiveaway.classes;

import me.clip.placeholderapi.PlaceholderAPI;
import me.msuro.mGiveaway.MGiveaway;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

/**
 * Represents a requirement for a giveaway.
 * It can be a permission, a role, or a number placeholder value
 * that must be met in order to participate in a giveaway.
 *
 * @param hasToBe true/false for permission and role
 *                true for number if the value has to be greater and false if it has to be equal or smaller
 *
 * @param amount the amount of the number placeholder value that must be met
 *
 * @param formatted the formatted string of the requirement
 *
 */
public record Requirement(String value, Requirement.Type type, boolean hasToBe, int amount, String formatted) {

    public boolean check(OfflinePlayer p) {
        if (p == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        Permission perms = MGiveaway.getInstance().getPerms();
        switch (type) {
            case PERMISSION:
                return perms.playerHas(null, p, value) == hasToBe;
            case ROLE:
                return Arrays.stream(perms.getPlayerGroups(null, p)).anyMatch(role -> role.equalsIgnoreCase(value)) == hasToBe;
            case NUMBER:
                try {
                    Double.parseDouble(PlaceholderAPI.setPlaceholders(p, "%" + value + "%"));
                } catch (NumberFormatException | NullPointerException e) {
                    return false;
                }
                double value = Double.parseDouble(PlaceholderAPI.setPlaceholders(p, "%"+this.value+"%"));
                return hasToBe ? value > amount : value <= amount;
            default:
                throw new IllegalArgumentException("Invalid requirement type");
        }
    }

    public String getFormatted() {
        return formatted;
    }

    public enum Type {
        PERMISSION,
        ROLE,
        NUMBER
    }

    public Requirement {
        if (value == null || type == null) {
            throw new IllegalArgumentException("Value and type cannot be null");
        }

    }

    public String toString() {
        return "Requirement{" +
                "value='" + value + '\'' +
                ", type=" + type +
                ", hasToBe=" + hasToBe +
                ", amount=" + amount +
                ", formatted='" + formatted + '\'' +
                '}';
    }

}
