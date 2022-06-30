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
import java.util.Objects;
import java.util.logging.Level;

/**
 * Listens for Paper's {@link PlayerHandshakeEvent} without authenticating sessions, which aren't verified when running
 * on Paper servers.
 */
public class PaperHandshakeListener extends AbstractHandshakeListener implements Listener {

    // Handshake field
    private Field originalHandshakeField;
    // Failed handshake
    private String failed = null;

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
            return;
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
            originalHandshakeField.set(event, result.getData());
            // Log
            logAuthResult(result);

            // If failed
            if (!result.getResult().isSuccess()) {
                fail(event);
                return;
            }

            // Set
            event.setServerHostname(result.getServerHostname());
            event.setSocketAddressHostname(result.getSocketAddressHostname());
            event.setUniqueId(Objects.requireNonNull(result.getUniqueId()));
            event.setPropertiesJson(result.getProperties());
            event.setCancelled(false);
        } catch (Exception ex) {
            // Log and cancel
            logAuthException(ex);
            fail(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandshakeManipulation(PlayerHandshakeEvent event) {
        // If cancelled
        if (event.isCancelled()) {
            getPlugin().getLogger().warning("A plugin cancelled the handshake event, bypassing SafeNET logic! Plugins should restrain from such behaviour due to several security reasons; report such usage to the developer. Shutting down...");
            Bukkit.shutdown();
            return;
        }

        // If not to fail
        if (failed == null || !failed.equals(event.getOriginalHandshake()))
            return;

        // If fail is revoked
        if (!event.isFailed()) {
            getPlugin().getLogger().warning("A plugin revoked fail of the handshake event! Plugins should restrain from such behaviour due to several security reasons; report such usage to the developer. Shutting down...");
            Bukkit.shutdown();
            return;
        }

        // Fail just in case
        event.setFailed(true);
        failed = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(PaperServerListPingEvent event) {
        if (isBlockPings())
            event.setCancelled(true);
    }

    /**
     * Fails the given event and monitors it.
     *
     * @param event the event to fail
     */
    @SuppressWarnings("deprecation")
    private void fail(@NotNull PlayerHandshakeEvent event) {
        // Fail
        event.setCancelled(false);
        event.setFailed(true);
        event.setFailMessage(getPlugin().getDisconnectHandler().getMessage());
        failed = event.getOriginalHandshake();
    }

}