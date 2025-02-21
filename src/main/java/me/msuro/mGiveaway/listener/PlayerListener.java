package me.msuro.mGiveaway.listener;

import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final MGiveaway instance;

    public PlayerListener() {
        this.instance = MGiveaway.getInstance();
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    }
}
