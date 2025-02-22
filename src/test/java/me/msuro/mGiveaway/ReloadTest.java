package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import me.msuro.mGiveaway.commands.Reload;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReloadTest {

    private MGiveaway plugin;
    private Reload reloadCommand;
    private CommandSender sender;
    private Command command;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        reloadCommand = new Reload();
        sender = mock(CommandSender.class);
        command = mock(Command.class);
        when(command.getName()).thenReturn("mgwreload");
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testReloadCommandExecution() {
        when(sender.hasPermission("mgiveaway.reload")).thenReturn(true);
        boolean result = reloadCommand.onCommand(sender, command, "mgwreload", new String[]{});
        assertTrue(result);
        verify(sender).sendMessage(TextUtil.process("%prefix% &7Reloading plugin..."));
        verify(sender).sendMessage(contains("Plugin reloaded!"));
    }

    @Test
    public void testReloadCommandNoPermission() {
        when(sender.hasPermission("mgiveaway.reload")).thenReturn(false);
        boolean result = reloadCommand.onCommand(sender, command, "mgwreload", new String[]{});
        assertTrue(result);
        verify(sender).sendMessage(TextUtil.process(ConfigUtil.getAndValidate(ConfigUtil.MESSAGES_IN_GAME_NO_PERMISSION)));
    }
}
