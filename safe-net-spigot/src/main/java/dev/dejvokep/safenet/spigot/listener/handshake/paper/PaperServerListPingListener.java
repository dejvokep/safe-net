/*
 * Copyright 2025 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.safenet.spigot.listener.handshake.paper;

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