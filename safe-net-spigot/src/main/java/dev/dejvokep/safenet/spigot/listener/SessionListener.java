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
package dev.dejvokep.safenet.spigot.listener;

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
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
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

    // Handlers field
    private Field handlersField;
    
    // Plugin
    private final SafeNetSpigot plugin;
    // Player that is kicked
    private Player kicked;

    /**
     * Registers the session listener.
     *
     * @param plugin the plugin
     */
    public SessionListener(SafeNetSpigot plugin) {
        // Set
        this.plugin = plugin;
        
        // Obtain the field
        try {
            handlersField = HandlerList.class.getDeclaredField("handlerslots");
            handlersField.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while obtaining reflection components to push the session listener to the first place! This might cause passphrase leaks if another plugins handle the exposed data incorrectly! Shutting down...");
            Bukkit.shutdown();
        }
        
        // Register
        Bukkit.getPluginManager().registerEvents(this, plugin);
        pushListener(PlayerJoinEvent.getHandlerList(), EventPriority.LOWEST);
        pushListener(PlayerKickEvent.getHandlerList(), EventPriority.MONITOR);
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

    /**
     * Pushes this listener to the first handler place for {@link PlayerJoinEvent}, so passphrase and session will be
     * cleared before it reaches other listeners.
     */
    private void pushListener(@NotNull HandlerList list, @NotNull EventPriority priority) {
        try {
            // Listeners on the lowest priority
            @SuppressWarnings("unchecked")
            ArrayList<RegisteredListener> listeners = ((EnumMap<EventPriority, ArrayList<RegisteredListener>>) handlersField.get(list)).get(priority);

            // Find the index of this listener
            int target = 0;
            for (; target < listeners.size(); target++) {
                if (listeners.get(target).getListener() == this)
                    break;
            }

            // Nothing to do
            if (target == 0)
                return;

            // Move all listeners one place behind
            RegisteredListener push = listeners.get(target);
            while (--target >= 0)
                listeners.set(target + 1, listeners.get(target));
            // Set this listener as first
            listeners.set(0, push);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while pushing the session listener to the first place! This might cause passphrase leaks if another plugins handle the exposed data incorrectly! Shutting down...", ex);
            Bukkit.shutdown();
        }
    }

}