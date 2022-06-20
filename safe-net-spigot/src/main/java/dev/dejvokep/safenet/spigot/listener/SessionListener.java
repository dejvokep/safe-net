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
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Session listener responsible for authenticating sessions.
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
    // Pending kick
    private final Set<Player> pendingKick = new HashSet<>();

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
        pushListener();
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
        plugin.getLogger().info(String.format(MESSAGE_DENIED, result.getCode(), player.getName(), player.getUniqueId(), result.getMessage()));

        // Disconnect
        try {
            plugin.getDisconnectHandler().play(player);
        } catch (Exception ex) {
            // Log
            plugin.getLogger().log(Level.SEVERE, "Failed to disconnect a player after failed session authentication! Attempting to use the server API.", ex);
            // If is offline
            if (!player.isOnline())
                return;
            // Kick
            pendingKick.add(player);
            player.kickPlayer(plugin.getDisconnectHandler().getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        // Revoke cancellation just in case
        if (pendingKick.remove(event.getPlayer()))
            event.setCancelled(false);
    }

    /**
     * Pushes this listener to the first handler place for {@link PlayerJoinEvent}, so passphrase and session will be
     * cleared before it reaches other listeners.
     */
    private void pushListener() {
        try {
            // Handlers field (no need to cache, one-time use only)
            Field handlersField = HandlerList.class.getDeclaredField("handlerslots");
            handlersField.setAccessible(true);

            // Listeners on the lowest priority
            @SuppressWarnings("unchecked")
            ArrayList<RegisteredListener> listeners = ((EnumMap<EventPriority, ArrayList<RegisteredListener>>) handlersField.get(PlayerJoinEvent.getHandlerList())).get(EventPriority.LOWEST);

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
            plugin.getLogger().log(Level.SEVERE, "An error occurred while pushing the session listener to the first place! This might cause passphrase leaks if another plugins handle the exposed data incorrectly!", ex);
        }
    }

}