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
package dev.dejvokep.securednetwork.bungeecord.listener;

import dev.dejvokep.securednetwork.bungeecord.SecuredNetworkBungeeCord;
import dev.dejvokep.securednetwork.bungeecord.ipwhitelist.AddressHolder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Class handling {@link LoginEvent} and {@link PostLoginEvent} events.
 * <ul>
 *     <li>{@link LoginEvent} processes the connection and inserts a special login result into the connection. This event
 *     was chosen because if the network is in online-mode, the player's textures (with the login profile) are set into the
 *     {@link InitialHandler}'s field just before the event fires - the old profile, which would be set before, if any of
 *     earlier events were used (e.g. {@link PreLoginEvent} for this task, would be overwritten.
 *     Therefore, this event is the first event that is suitable.</li>
 *     <li>{@link PostLoginEvent} sends players with the updater permission an updater message.</li>
 * </ul>
 */
public class LoginListener implements Listener {

    /**
     * Default disconnect message used if the one provided in the config is invalid.
     */
    private static final String DEFAULT_DISCONNECT_MESSAGE = "Disconnected";

    /**
     * If the cancel reason is set using a string - if using server version <code>1.7</code>.
     */
    private static final boolean OLD_CANCEL_REASON = ProxyServer.getInstance().getVersion().contains("1.7");

    // Login result field
    private Field loginResultField;
    // The plugin instance
    private final SecuredNetworkBungeeCord plugin;

    // The passphrase error message
    private TextComponent disconnectMessage;

    /**
     * Utilizes the login result field of the {@link InitialHandler} class. Loads the passphrase error disconnect
     * message.
     *
     * @param plugin the plugin instance
     */
    public LoginListener(@NotNull SecuredNetworkBungeeCord plugin) {
        // Set
        this.plugin = plugin;
        // Reload
        reload();

        try {
            // Get the field
            loginResultField = InitialHandler.class.getDeclaredField("loginProfile");
            // Set accessible
            loginResultField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            // Log
            plugin.getLogger().log(Level.SEVERE, "Failed to utilize the loginProfile field!", ex);
        }
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        // Player
        ProxiedPlayer player = event.getPlayer();

        // If does not have the permission
        if (!player.hasPermission("secured-network.updater") && !player.hasPermission("secured-network.*"))
            return;
        // Send the message
        plugin.getMessenger().sendMessage(player, plugin.getUpdater().getJoinMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(LoginEvent event) {
        // The connection
        PendingConnection connection = event.getConnection();
        // Virtual host address
        String virtualHost = connection.getVirtualHost().getHostString() + AddressHolder.PORT_COLON + connection.getVirtualHost().getPort();
        // Connection name
        String name = connection.getName();
        // Check the address
        boolean result = plugin.getAddressWhitelist().verifyAddress(connection.getVirtualHost());

        // If not passed
        if (!result) {
            // Rejected
            plugin.getLogger().info(String.format("ERROR (code P1): Rejected connection of name \"%s\" by address whitelist; used address was %s. Didn't you mean to whitelist it?", name, virtualHost));
            cancel(event);
            return;
        }

        // If the passphrase is valid
        boolean validPassphrase = plugin.getAuthenticator().getPassphrase() != null && plugin.getAuthenticator().getPassphrase().length() > 0;
        // Insert the custom result
        if (validPassphrase && setResult(event.getConnection())) {
            // Accepted
            plugin.getLogger().info(String.format("OK (code P0): Accepted connection of \"%s\".", name));
        } else {
            // Rejected
            plugin.getLogger().info(String.format("ERROR (code P%d): Rejected connection of \"%s\"; %s", validPassphrase ? 3 : 2, name, validPassphrase ? "failed to process. Please check for any errors." : "passphrase is not configured."));
            cancel(event);
        }
    }

    /**
     * Reloads the internal data.
     */
    public void reload() {
        // Set
        disconnectMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getString("disconnect-message", DEFAULT_DISCONNECT_MESSAGE)));
    }

    /**
     * Sets a custom login result (with the passphrase) into the given connection's {@link LoginResult}.
     *
     * @param pendingConnection the connection to insert the custom result into
     * @return if the process finished without any error
     */
    private boolean setResult(@NotNull PendingConnection pendingConnection) {
        // If null
        if (loginResultField == null)
            // Should not happen
            return false;

        try {
            // Set
            loginResultField.set(pendingConnection, new SecuredLoginResult(((InitialHandler) pendingConnection).getLoginProfile(), plugin.getAuthenticator()));
        } catch (Exception ex) {
            // Log
            plugin.getLogger().log(Level.SEVERE, "An error occurred while setting the custom login result into the connection!", ex);
            return false;
        }

        // Did not fail
        return true;
    }

    /**
     * Cancels a event and sets a disconnect message.
     *
     * @param event the event to cancel
     */
    private void cancel(@NotNull LoginEvent event) {
        // Set the message
        if (OLD_CANCEL_REASON)
            event.setCancelReason(disconnectMessage.getText());
        else
            event.setCancelReason(disconnectMessage);

        // Cancel the event
        event.setCancelled(true);
    }
}