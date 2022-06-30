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

import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import dev.dejvokep.safenet.spigot.authentication.result.HandshakeAuthenticationResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;

/**
 * Abstraction for handshake listeners.
 */
public abstract class AbstractHandshakeListener {

    /**
     * Message logged when a connection was denied.
     */
    private static final String MESSAGE_DENIED = "DENIED (code B%d): Failed to authenticate handshake \"%s\": %s Data: %s";

    /**
     * Message logged when a connection was accepted.
     */
    private static final String MESSAGE_ACCEPTED = "ACCEPTED (code B%d): Authenticated \"%s\".";

    // Plugin
    private final SafeNetSpigot plugin;
    // If to block pings
    private boolean blockPings;

    public AbstractHandshakeListener(SafeNetSpigot plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reloads internal configuration.
     */
    public void reload() {
        this.blockPings = plugin.getConfiguration().getBoolean("block-pings");

        // Warn
        if (blockPings)
            plugin.getLogger().warning("Server pinging is blocked. Please note that this may break functionality of plugins that rely on pings to obtain server information.");
    }

    /**
     * Logs the given authentication result.
     *
     * @param result the result
     */
    public void logAuthResult(HandshakeAuthenticationResult result) {
        if (result.getResult().isSuccess())
            plugin.getLogger().info(String.format(MESSAGE_ACCEPTED, result.getResult().getCode(), result.getUniqueIdAsString()));
        else
            plugin.getLogger().warning(String.format(MESSAGE_DENIED, result.getResult().getCode(), result.getUniqueIdAsString(), result.getResult().getMessage(), Base64.getEncoder().encodeToString(result.getData().getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Logs the given authentication exception.
     *
     * @param ex the exception
     */
    public void logAuthException(Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "An error occurred whilst processing a handshake!", ex);
    }

    /**
     * Returns the plugin instance.
     *
     * @return the plugin instance
     */
    public SafeNetSpigot getPlugin() {
        return plugin;
    }

    /**
     * Returns whether to block pings.
     *
     * @return whether to block pings
     */
    public boolean isBlockPings() {
        return blockPings;
    }
}