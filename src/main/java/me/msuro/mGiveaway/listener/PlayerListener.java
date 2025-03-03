package me.msuro.mGiveaway.listener;

import me.msuro.mGiveaway.Giveaway;
import me.msuro.mGiveaway.MGiveaway;
import me.msuro.mGiveaway.utils.ConfigUtil;
import me.msuro.mGiveaway.utils.GiveawayManager;
import me.msuro.mGiveaway.utils.TextUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerListener implements Listener {

    private final MGiveaway instance;

    public PlayerListener() {
        instance = MGiveaway.getInstance();
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println("Player joinedv1");
        instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                GiveawayManager manager = instance.getGiveawayManager();
                for(Giveaway giveaway : manager.listGiveaways().values()) {
                    if(giveaway.state() == Giveaway.State.STARTED) {
                        String message = TextUtil.process(ConfigUtil.getAndValidate(ConfigUtil.GIVEAWAY_INFO_PERSONAL_ON_JOIN)
                                .replace("%name%", giveaway.name())
                                .replace("%prize%", giveaway.prize())
                                .replace("%time_left%", giveaway.getTimeLeft())
                                .replace("%winners%", String.valueOf(giveaway.winCount())));
                        event.getPlayer().sendMessage(message);


                    }
                }
            }
        }, 20L);
    }
}
