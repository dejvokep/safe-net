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

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Stores a whitelisted IP (IPv4 format) and covers basic manipulation methods.
 */
public class AddressHolder {

    /**
     * IP (IPv4) address pattern.
     */
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("!?(?:[^.]+\\.)+.+:[0-9]{1,5}");

    /**
     * The case-sensitive pattern.
     */
    private static final Pattern CASE_SENSITIVE_PATTERN = Pattern.compile("!.+");

    /**
     * The port colon.
     */
    public static final char PORT_COLON = ':';

    // The address
    private String address;
    // If to compare case-sensitively
    private boolean caseSensitive;

    /**
     * Compares the address held with the given one and returns whether they are the same.
     *
     * @param address the address to compare, including the port in format <code>IP:port</code>
     * @return whether the given address equals the held address (if they are equal and the player connecting with such
     * address is allowed to connect to the server)
     */
    public boolean compare(@NotNull String address) {
        return caseSensitive ? this.address.equals(address) : this.address.equalsIgnoreCase(address);
    }

    /**
     * Sets the address held by this instance to the specified one (may include the case-sensitivity surrounding, must
     * include a port separated by {@link #PORT_COLON}).
     * <p>
     * Returns if the operation was successful - if the address is correctly specified (matches the {@link #ADDRESS_PATTERN}
     * pattern).
     *
     * @param address the address to be held
     * @return if the address is valid and was successfully set
     */
    public boolean setAddress(@NotNull String address) {
        // Replace all the spaces
        address = address.replace(" ", "");
        // Match the address pattern
        if (!ADDRESS_PATTERN.matcher(address).matches())
            return false;

        // Case-sensitive depending on the match
        caseSensitive = CASE_SENSITIVE_PATTERN.matcher(address).matches();
        // Remove the indicator
        this.address = caseSensitive ? address.substring(1) : address;
        return true;
    }

    /**
     * Returns the address held by this instance of this class, including the case-sensitive indicator.
     *
     * @return the address held
     */
    public String getAddress() {
        return caseSensitive ? "!" + address : address;
    }

}