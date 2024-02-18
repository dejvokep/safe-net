/*
 * Copyright 2024 https://dejvokep.dev/
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

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import dev.dejvokep.safenet.spigot.authentication.result.HandshakeAuthenticationResult;
import dev.dejvokep.safenet.spigot.listener.handshake.AbstractHandshakeListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Listens for Paper's {@link PlayerHandshakeEvent} without authenticating sessions. On Paper servers, packets are held
 * until all plugins are initialized, so the listener is guaranteed to always be registered and therefore no session
 * validation is needed.
 */
public class PaperHandshakeListener extends AbstractHandshakeListener implements Listener {

    /**
     * Paper's server list ping event class.
     */
    private static final String PAPER_SERVER_LIST_PING_EVENT = "com.destroystokyo.paper.event.server.PaperServerListPingEvent";

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
        super(plugin, plugin.classExists(PAPER_SERVER_LIST_PING_EVENT));

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

        // Register server list ping listener only if available
        if (isPingBlockingAvailable())
            new PaperServerListPingListener(plugin);
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

    @Override
    public boolean isCombined() {
        return false;
    }
}