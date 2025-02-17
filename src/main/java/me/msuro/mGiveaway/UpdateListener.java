package me.msuro.mGiveaway;

import com.jeff_media.updatechecker.UpdateCheckEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UpdateListener implements Listener {
    private final MGiveaway instance;

    public UpdateListener() {
        this.instance = MGiveaway.getInstance();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    //todo
    @EventHandler
    public void onUpdateCheck(UpdateCheckEvent event) {
        //System.out.println(event.getLatestVersion() + " is the latest version!");
        //System.out.println(event.getUsedVersion() + " is the version you are using!");
        //System.out.println(event.getResult());
    }
}
