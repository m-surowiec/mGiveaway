package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.msuro.mGiveaway.utils.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigUtilTest {

    private MGiveaway plugin;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testConfigLoading() {
        ConfigUtil configUtil = new ConfigUtil();
        YamlConfiguration config = ConfigUtil.getConfig();
        assertNotNull(config);
        assertEquals("0.1", config.getString(ConfigUtil.CONFIG_VERSION));
    }

    @Test
    public void testConfigSaving() {
        ConfigUtil configUtil = new ConfigUtil();
        YamlConfiguration config = ConfigUtil.getConfig();
        config.set("test_key", "test_value");
        ConfigUtil.saveConfig();
        YamlConfiguration reloadedConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        assertEquals("test_value", reloadedConfig.getString("test_key"));
    }

    @Test
    public void testConfigValidation() {
        ConfigUtil configUtil = new ConfigUtil();
        String value = ConfigUtil.getAndValidate(ConfigUtil.PREFIX);
        assertNotNull(value);
        assertNotEquals("XXX", value);
    }

    @Test
    public void testConfigUpdate() {
        ConfigUtil configUtil = new ConfigUtil();
        YamlConfiguration config = ConfigUtil.getConfig();
        config.set(ConfigUtil.CONFIG_VERSION, "0.4");
        ConfigUtil.saveConfig();
        ConfigUtil.updateConfig();
        assertEquals("0.7", config.getString(ConfigUtil.CONFIG_VERSION));
    }
}
