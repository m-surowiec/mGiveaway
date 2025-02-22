package me.msuro.mGiveaway;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UpdateCheckerTest {

    private MGiveaway plugin;
    private UpdateChecker updateChecker;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(MGiveaway.class);
        updateChecker = UpdateChecker.init(plugin, 122302);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testUpdateCheck() {
        CompletableFuture<UpdateChecker.UpdateResult> future = updateChecker.requestUpdateCheck();
        UpdateChecker.UpdateResult result = future.join();
        assertNotNull(result);
        assertEquals(UpdateChecker.UpdateReason.UP_TO_DATE, result.getReason());
    }

    @Test
    public void testVersionComparison() {
        String currentVersion = "1.0.0";
        String newVersion = "1.1.0";
        String latest = UpdateChecker.VERSION_SCHEME_DECIMAL.compareVersions(currentVersion, newVersion);
        assertEquals(newVersion, latest);
    }
}
