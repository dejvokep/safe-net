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
package dev.dejvokep.safenet.bungeecord.listener;

import dev.dejvokep.safenet.bungeecord.SafeNetBungeeCord;
import dev.dejvokep.safenet.bungeecord.ipwhitelist.AddressHolder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
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
     * Message logged when a connection was denied.
     */
    private static final String MESSAGE_DENIED = "DENIED (code P%d): Failed to process \"%s\": %s";
    /**
     * Message logged when a connection was accepted.
     */
    private static final String MESSAGE_ACCEPTED = "ACCEPTED (code P%d): Processed \"%s\".";

    /**
     * Code meaning an accepted connection.
     */
    private static final int CODE_ACCEPTED = 0;
    /**
     * Code meaning a denied connection because of the address whitelist.
     */
    private static final int CODE_DENIED_ADDRESS_WHITELIST = 1;
    /**
     * Code meaning a denied connection because no passphrase is configured.
     */
    private static final int CODE_DENIED_PASSPHRASE_NOT_CONFIGURED = 2;
    /**
     * Code meaning a denied connection because of an unknown error.
     */
    private static final int CODE_DENIED_UNKNOWN_ERROR = 3;

    /**
     * Reason for denying a connection because of the address whitelist.
     */
    private static final String REASON_ADDRESS_WHITELIST = "used address %s is not whitelisted. Didn't you mean to whitelist it?";
    /**
     * Reason for denying a connection because of an unknown error.
     */
    private static final String REASON_UNKNOWN_ERROR = "an unknown error occurred.";
    /**
     * Reason for denying a connection because no passphrase is configured.
     */
    private static final String REASON_PASSPHRASE_NOT_CONFIGURED = "passphrase is not configured.";

    // Login result field
    private Field loginResultField;
    // The plugin instance
    private final SafeNetBungeeCord plugin;

    // The passphrase error message
    private TextComponent disconnectMessage;

    /**
     * Utilizes the login result field of the {@link InitialHandler} class. Loads the passphrase error disconnect
     * message.
     *
     * @param plugin the plugin instance
     */
    public LoginListener(@NotNull SafeNetBungeeCord plugin) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(LoginEvent event) {
        // The connection
        PendingConnection connection = event.getConnection();
        // Virtual host address
        String virtualHost = connection.getVirtualHost().getHostString() + AddressHolder.PORT_COLON + connection.getVirtualHost().getPort();
        // Connection name
        String name = connection.getName();

        // If not passed
        if (!plugin.getAddressWhitelist().verifyAddress(connection.getVirtualHost())) {
            // Rejected
            plugin.getLogger().info(String.format(MESSAGE_DENIED, CODE_DENIED_ADDRESS_WHITELIST, name, String.format(REASON_ADDRESS_WHITELIST, virtualHost)));
            cancel(event);
            return;
        }

        // If the passphrase is valid
        boolean validPassphrase = plugin.getAuthenticator().getPassphrase() != null && plugin.getAuthenticator().getPassphrase().length() > 0;
        // Insert the custom result
        if (validPassphrase && setResult(event.getConnection())) {
            // Accepted
            plugin.getLogger().info(String.format(MESSAGE_ACCEPTED, CODE_ACCEPTED, name));
        } else {
            // Rejected
            plugin.getLogger().info(String.format(MESSAGE_DENIED, validPassphrase ? CODE_DENIED_UNKNOWN_ERROR : CODE_DENIED_PASSPHRASE_NOT_CONFIGURED, name, validPassphrase ? REASON_UNKNOWN_ERROR : REASON_PASSPHRASE_NOT_CONFIGURED));
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
            loginResultField.set(pendingConnection, new CustomLoginResult(((InitialHandler) pendingConnection).getLoginProfile(), plugin.getAuthenticator()));
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
        // Cancel the event
        event.setCancelled(true);
        // Set the message
        event.setCancelReason(disconnectMessage);
    }
}