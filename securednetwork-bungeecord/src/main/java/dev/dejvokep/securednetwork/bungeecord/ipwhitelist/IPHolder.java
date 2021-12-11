/*
 * Copyright 2021 https://dejvokep.dev/
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
package dev.dejvokep.securednetwork.bungeecord.ipwhitelist;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Stores a whitelisted IP (IPv4 format) and covers basic manipulation methods.
 */
public class IPHolder {

    /**
     * IP (IPv4) address pattern.
     */
    private static final Pattern IP_PATTERN = Pattern.compile("((?:[^.]+\\.)+.+:[0-9]{1,5})|(\\[(?:[^.]+\\.)+.+:[0-9]{1,5}])");

    /**
     * The case-insensitive pattern.
     */
    private static final Pattern CASE_INSENSITIVE_PATTERN = Pattern.compile("\\[.+]");

    /**
     * The port colon.
     */
    public static final char PORT_COLON = ':';

    // The IP
    private String ip;
    // If to compare case-sensitively
    private boolean caseSensitive;

    /**
     * Compares the IP held with the given IP and returns whether they are the same.
     *
     * @param ip the IP to compare including the port in format <code>IP:port</code>
     * @return whether the given IP equals the held IP (if they are equal and the player connecting with such IP is
     * allowed to connect to the server)
     */
    public boolean compare(@NotNull String ip) {
        return caseSensitive ? this.ip.equals(ip) : this.ip.equalsIgnoreCase(ip);
    }

    /**
     * Sets the IP held by this instance to the specified one (may include the case-insensitivity surrounding, must
     * include a port separated by {@link #PORT_COLON}).
     * <p>
     * Returns if the operation was successful - if the IP is correctly specified (matches the {@link #IP_PATTERN} pattern).
     *
     * @param ip the IP to be held
     * @return if the IP is valid and was successfully set
     */
    public boolean setIp(@NotNull String ip) {
        // Replace all the spaces
        ip = ip.replace(" ", "");
        // Match the IP pattern
        if (!IP_PATTERN.matcher(ip).matches())
            return false;

        // Set the case-insensitive depending on the pattern match
        caseSensitive = !CASE_INSENSITIVE_PATTERN.matcher(ip).matches();
        // Set
        this.ip = caseSensitive ? ip : ip.substring(1, ip.length() - 1);
        return true;
    }

    /**
     * Returns the IP held by this instance of this class, including the case-insensitive brackets.
     *
     * @return the IP held
     */
    public String getIp() {
        return caseSensitive ? ip : "[" + ip + "]";
    }

    /**
     * Returns if this IP is case-sensitive.
     *
     * @return if this IP is case-sensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

}