package dev.dejvokep.safenet.spigot.listener.handshake;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens for Paper's {@link PaperServerListPingEvent}.
 */
public class PaperServerListPingListener implements Listener {

    // Plugin
    private final SafeNetSpigot plugin;

    /**
     * Registers the server list ping listener.
     *
     * @param plugin the plugin
     */
    public PaperServerListPingListener(SafeNetSpigot plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(PaperServerListPingEvent event) {
        if (plugin.getHandshakeListener().isBlockPings())
            event.setCancelled(true);
    }
}