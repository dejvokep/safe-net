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
package dev.dejvokep.safenet.spigot.listener.handshake;

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import dev.dejvokep.safenet.spigot.authentication.result.HandshakeAuthenticationResult;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Listens for Paper's {@link PlayerHandshakeEvent} without authenticating sessions, which aren't verified when running
 * on Paper servers.
 */
public class PaperHandshakeListener extends AbstractHandshakeListener implements Listener {

    // Handshake field
    private Field originalHandshakeField;
    // Cancelled handshake
    private String cancelled = null;

    /**
     * Registers the handshake listener.
     *
     * @param plugin the plugin
     */
    public PaperHandshakeListener(SafeNetSpigot plugin) {
        super(plugin);

        // Obtain the field
        try {
            originalHandshakeField = PlayerHandshakeEvent.class.getDeclaredField("originalHandshake");
            originalHandshakeField.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst obtaining reflection components to replace handshake data! This might cause passphrase leaks if another plugins handle the exposed data incorrectly! Shutting down...");
            Bukkit.shutdown();
        }

        // Register
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getEventPusher().push(PlayerHandshakeEvent.getHandlerList(), EventPriority.LOWEST, this);
        plugin.getEventPusher().push(PlayerHandshakeEvent.getHandlerList(), EventPriority.MONITOR, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHandshake(PlayerHandshakeEvent event) {
        try {
            // Authenticate
            HandshakeAuthenticationResult result = getPlugin().getAuthenticator().handshake(event.getOriginalHandshake());
            // Replace
            replaceHandshake(event, result.getHost());
            // Log
            logAuthResult(result);

            // If failed
            if (!result.getResult().isSuccess())
                cancel(event);

        } catch (Exception ex) {
            // Log and cancel
            logAuthException(ex);
            cancel(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandshakeCancel(PlayerHandshakeEvent event) {
        // If not to cancel
        if (cancelled != null && !cancelled.equals(event.getOriginalHandshake()))
            return;

        // If the cancellation is revoked
        if (!event.isCancelled()) {
            getPlugin().getLogger().warning("A plugin revoked cancellation of the handshake event! Plugins should restrain from such behaviour due to several security reasons; report such usage to the developer. Shutting down...");
            Bukkit.shutdown();
            return;
        }

        // Cancel just in case
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(PaperServerListPingEvent event) {
        if (isBlockPings())
            event.setCancelled(true);
    }

    /**
     * Replaces the given event's handshake.
     *
     * @param event     event whose handshake to replace
     * @param handshake the new handshake
     */
    private void replaceHandshake(@NotNull PlayerHandshakeEvent event, @NotNull String handshake) {
        try {
            // Replace
            originalHandshakeField.set(event, handshake);
        } catch (ReflectiveOperationException ex) {
            getPlugin().getLogger().log(Level.SEVERE, "An error occurred whilst replacing handshake data!", ex);
            cancel(event);
        }
    }

    /**
     * Cancels the given event and monitors it.
     *
     * @param event the event to cancel
     */
    private void cancel(@NotNull PlayerHandshakeEvent event) {
        // Cancel
        event.setCancelled(true);
        cancelled = event.getOriginalHandshake();
    }

}