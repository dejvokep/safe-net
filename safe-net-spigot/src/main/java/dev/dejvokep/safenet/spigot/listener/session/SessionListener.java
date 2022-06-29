/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.safenet.spigot.listener.session;

import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import dev.dejvokep.safenet.spigot.authentication.result.AuthenticationResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

/**
 * Session listener responsible for authenticating sessions and appropriately disconnecting unauthorized connections.
 */
public class SessionListener implements Listener {

    /**
     * Message logged when a connection was denied.
     */
    private static final String MESSAGE_DENIED = "DENIED (code B%d): Failed to authenticate session \"%s\" (%s): %s";

    /**
     * Message logged when a connection was accepted.
     */
    private static final String MESSAGE_ACCEPTED = "ACCEPTED (code B%d): Authenticated \"%s\" (%s).";
    
    // Plugin
    private final SafeNetSpigot plugin;
    // Kicked player
    private Player kicked = null;

    /**
     * Registers the session listener.
     *
     * @param plugin the plugin
     */
    public SessionListener(SafeNetSpigot plugin) {
        // Set
        this.plugin = plugin;
        
        // Register
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getEventPusher().push(PlayerJoinEvent.getHandlerList(), EventPriority.LOWEST, this);
        plugin.getEventPusher().push(PlayerKickEvent.getHandlerList(), EventPriority.MONITOR, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        // Player
        Player player = event.getPlayer();
        // Authenticate
        AuthenticationResult result = plugin.getAuthenticator().session(player);

        // If is accepted
        if (result.isSuccess()) {
            plugin.getLogger().info(String.format(MESSAGE_ACCEPTED, result.getCode(), player.getName(), player.getUniqueId()));
            return;
        }

        // Log
        plugin.getLogger().warning(String.format(MESSAGE_DENIED, result.getCode(), player.getName(), player.getUniqueId(), result.getMessage()));

        // Kick
        kicked = player;
        plugin.getDisconnectHandler().play(player);
        // If null, player got disconnected
        if (kicked == null)
            return;

        // Log
        plugin.getLogger().log(Level.SEVERE, String.format("Failed to disconnect player \"%s\" (%s), because a plugin cancelled the kick event on MONITOR priority! Plugins should restrain from such behaviour due to several security reasons and API principles; report such usage to the developer. Shutting down...", player.getName(), player.getUniqueId()));
        Bukkit.shutdown();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        // If not to kick
        if (kicked != event.getPlayer())
            return;

        // Revoke cancellation just in case
        event.setCancelled(false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Disconnected
        if (kicked == event.getPlayer())
            kicked = null;
    }

}