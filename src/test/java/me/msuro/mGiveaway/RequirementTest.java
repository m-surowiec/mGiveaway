package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RequirementTest {

    private MGiveaway plugin;
    private Permission perms;
    private OfflinePlayer player;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        perms = mock(Permission.class);
        player = mock(OfflinePlayer.class);
        when(plugin.getPerms()).thenReturn(perms);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPermissionRequirement() {
        Requirement requirement = new Requirement("example.permission", Requirement.Type.PERMISSION, true, 0, "Example Permission");
        when(perms.playerHas(null, player, "example.permission")).thenReturn(true);
        assertTrue(requirement.check(player));
    }

    @Test
    public void testRoleRequirement() {
        Requirement requirement = new Requirement("example.role", Requirement.Type.ROLE, true, 0, "Example Role");
        when(perms.getPlayerGroups(null, player)).thenReturn(new String[]{"example.role"});
        assertTrue(requirement.check(player));
    }

    @Test
    public void testNumberRequirement() {
        Requirement requirement = new Requirement("example.number", Requirement.Type.NUMBER, true, 10, "Example Number");
        when(PlaceholderAPI.setPlaceholders(player, "%example.number%")).thenReturn("15");
        assertTrue(requirement.check(player));
    }

    @Test
    public void testNullPlayerRequirement() {
        Requirement requirement = new Requirement("example.permission", Requirement.Type.PERMISSION, true, 0, "Example Permission");
        assertThrows(IllegalArgumentException.class, () -> requirement.check(null));
    }
}
