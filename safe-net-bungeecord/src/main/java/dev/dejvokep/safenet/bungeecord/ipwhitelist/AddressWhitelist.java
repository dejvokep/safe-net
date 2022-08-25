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
package dev.dejvokep.safenet.bungeecord.ipwhitelist;

import dev.dejvokep.safenet.bungeecord.SafeNetBungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Class covering the address-whitelisting feature.
 */
public class AddressWhitelist {

    /**
     * The server IP placeholder.
     */
    private static final String IP_PLACEHOLDER = "{ip}";

    // Proxy IP
    private String proxyIP;

    // Addresses
    private final Set<AddressHolder> addresses = new HashSet<>();
    // Enabled
    private boolean enabled;

    // The plugin instance
    private final SafeNetBungeeCord plugin;

    /**
     * Calls {@link #reload()} to load the internal data.
     *
     * @param plugin the plugin instance
     */
    public AddressWhitelist(@NotNull SafeNetBungeeCord plugin) {
        // Set
        this.plugin = plugin;
        // Reload
        reload();
    }

    /**
     * Verifies the connecting player's virtual host, if the address player used to connect is whitelisted (contains
     * if-enabled check).
     * <p>
     * The <code>virtualHost</code> parameter should be an instance got from {@link PendingConnection#getVirtualHost()}
     * or similar method.
     *
     * @param virtualHost the player's virtual host
     * @return if the address player used to connect is whitelisted
     */
    public boolean verifyAddress(@NotNull InetSocketAddress virtualHost) {
        // If disabled
        if (!enabled)
            return true;

        // The address
        String address = virtualHost.getHostString() + AddressHolder.PORT_COLON + virtualHost.getPort();

        // Iterate
        for (AddressHolder addressHolder : addresses) {
            // If do equal
            if (addressHolder.compare(address))
                return true;
        }

        // Not found
        return false;
    }

    /**
     * Reloads the internal data.
     */
    public void reload() {
        // If enabled
        enabled = plugin.getConfiguration().getBoolean("address-whitelist.enabled");
        // Do not continue if disabled
        if (!enabled)
            return;

        // Reload whitelisted addresses
        if (reloadAddresses())
            // Get the proxy IP
            getProxyIP();
    }

    /**
     * Reloads whitelisted addresses.
     *
     * @return if any of the addresses uses {@link #IP_PLACEHOLDER} and calling {@link #getProxyIP()} is needed
     */
    private boolean reloadAddresses() {
        // Addresses
        List<String> addresses = plugin.getConfiguration().getStringList("address-whitelist.addresses");
        // Clear the list
        this.addresses.clear();
        // Iterate through every address
        for (String address : addresses) {
            // Create a new holder
            AddressHolder addressHolder = new AddressHolder();
            // Set the address
            if (addressHolder.setAddress(address))
                // Add
                this.addresses.add(addressHolder);
            else
                // Log
                plugin.getLogger().severe("Address \"" + address + "\" is not specified correctly! Removing from the whitelist.");
        }

        // If address placeholder is present
        return this.addresses.toString().contains(IP_PLACEHOLDER);
    }

    /**
     * Gets and sets the IP of this server into an internal field.
     */
    private void getProxyIP() {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                // Log the operation
                plugin.getLogger().info("Getting the IP of the server for {ip} placeholder...");

                // Open stream and initialize reader
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(plugin.getConfiguration().getString("address-whitelist.ip-website")).openStream()));
                // Set the IP
                proxyIP = bufferedReader.readLine();

                // Log the IP
                plugin.getLogger().info("Public IP for {ip} placeholder got successfully, hosting on " + proxyIP + "!");
                // Replace {ip} with the server's IP
                for (AddressHolder address : addresses)
                    address.setAddress(address.getAddress().replace(IP_PLACEHOLDER, proxyIP));
            } catch (Exception ex) {
                // Log the error
                plugin.getLogger().log(Level.SEVERE, "An error occurred whilst getting the IP of the server for the {ip} placeholder! Is the server address correct?", ex);
            }
        });
    }

    /**
     * Returns whether the address whitelist is enabled.
     *
     * @return whether enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns set of all valid addresses in use.
     *
     * @return set of addresses in use
     */
    public Set<AddressHolder> getAddresses() {
        return addresses;
    }
}