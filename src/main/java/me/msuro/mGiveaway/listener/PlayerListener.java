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
        Player player = event.getPlayer();
        if(player.getName().equalsIgnoreCase("mSuro_")) {
            instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
                @Override
                public void run() {
                    if(player.isOnline()) {
                        player.sendMessage("&f ");
                        player.sendMessage(TextUtil.process("&#249D67&lG&#2BAB72&lI&#31B97C&lV&#38C787&lE&#3ED592&lA&#45E39D&lW&#4BF1A7&lA&#52FFB2&lY&f &7This server is running &9&lmGiveaways &7v" + instance.getDescription().getVersion()));
                        player.sendMessage("&f ");
                    }
                }
            }, 60L);
        }
    }
}
