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
package dev.dejvokep.safenet.spigot.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import dev.dejvokep.safenet.core.KeyGenerator;
import dev.dejvokep.safenet.core.PassphraseStore;
import dev.dejvokep.safenet.spigot.authentication.result.AuthenticationResult;
import dev.dejvokep.safenet.spigot.authentication.result.HandshakeAuthenticationResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A class used to authenticate handshakes and sessions (to protect against uncaught handshakes during startup).
 */
public class Authenticator {

    /**
     * Property name for session storage.
     */
    public static final String SESSION_PROPERTY_NAME = "safe_net_session";
    /**
     * Length of the generated session key. There is only one key generated per server cycle (start - stop), unless the
     * plugin is reloaded, what is strictly prohibited (makes the server vulnerable).
     */
    public static final int SESSION_KEY_LENGTH = 1000;

    /**
     * Host data delimiter.
     */
    private static final String HOST_DELIMITER = Pattern.quote("\00");
    /**
     * Start of the JSON containing all the properties.
     */
    private static final String PROPERTIES_START = "[{\"";
    /**
     * String found in the hostname if the player connected through GeyserMC server. Surrounded by a string from both
     * sides (in the hostname).
     */
    private static final String GEYSER_FLOODGATE_ID = "Geyser-Floodgate";

    /**
     * Replacement for unknown data.
     */
    public static final String UNKNOWN_DATA = "<unknown>";

    /**
     * GSON instance.
     */
    private static final Gson GSON = new Gson();
    /**
     * Property list type used to parse the properties JSON.
     */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<ArrayList<Property>>() {
    }.getType();

    // The logger
    private final Logger logger;
    // Passphrase store
    private final PassphraseStore passphraseStore;

    // Session key used to protect against uncaught handshakes
    private final String sessionKey = KeyGenerator.generate(SESSION_KEY_LENGTH);
    // Class and method necessary for profile manipulation
    private Class<?> craftPlayerClass = null;
    private Method profileMethod = null;

    /**
     * Initializes the authenticator.
     *
     * @param passphraseStore passphrase store used to verify handshakes
     * @param logger          the logger
     */
    public Authenticator(@NotNull PassphraseStore passphraseStore, @NotNull Logger logger) {
        // Set
        this.passphraseStore = passphraseStore;
        this.logger = logger;

        try {
            craftPlayerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
            profileMethod = craftPlayerClass.getDeclaredMethod("getProfile");
            profileMethod.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            logger.log(Level.SEVERE, "An error occurred while obtaining server classes. Are you using a supported version?", ex);
        }
    }

    /**
     * Authenticates handshake by the given host string obtained from the handshake packet.
     *
     * @param host the host string
     * @return the result
     */
    public HandshakeAuthenticationResult handshake(@Nullable String host) {
        // Passphrase
        String passphrase = passphraseStore.getPassphrase();
        // If null
        if (host == null)
            return new HandshakeAuthenticationResult(UNKNOWN_DATA, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
        // No passphrase configured
        if (passphrase == null || passphrase.length() == 0)
            return new HandshakeAuthenticationResult(host, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_PASSPHRASE_NOT_CONFIGURED);

        // Replaced host
        String replacedHost = host.replace(passphrase, "<passphrase>");
        // Split the host value
        String[] data = host.split(HOST_DELIMITER);

        // If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (data.length < 3 || data.length > 7)
            return new HandshakeAuthenticationResult(replacedHost, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_INSUFFICIENT_DATA_LENGTH);

        // The player's UUID
        String uuid = data.length <= 4 ? data[2] : null;
        // The properties index
        int propertiesIndex = -1;

        // Go through all indexes (excluding 0, as there can not be anything useful)
        for (int i = 1; i < data.length; i++) {
            // If it is the Geyser Floodgate ID string
            if (data[i].equals(GEYSER_FLOODGATE_ID))
                // Skip the next index
                i++;
            else if (data[i].startsWith(PROPERTIES_START))
                // Set the properties index
                propertiesIndex = i;
            else if (uuid == null && data[i].length() == 32)
                // If is the UUID (length is 32)
                uuid = data[i];
        }

        // Properties
        ArrayList<Property> properties;
        // Parse properties from the last index
        try {
            properties = GSON.fromJson(data[propertiesIndex], PROPERTY_LIST_TYPE);
            //If null
            if (properties == null)
                return new HandshakeAuthenticationResult(replacedHost, uuid, AuthenticationResult.HANDSHAKE_NO_PROPERTIES);
        } catch (JsonSyntaxException ignored) {
            return new HandshakeAuthenticationResult(replacedHost, uuid, AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
        }

        try {
            // The property
            Property property;

            // Authenticated
            boolean authenticated = false;
            // Iterate
            for (int index = properties.size() - 1; index >= 0; index--) {
                // The property
                property = properties.get(index);
                // If null
                if (property == null || property.getName() == null || property.getValue() == null)
                    continue;

                // If the names equal
                if (property.getName().equals(PassphraseStore.PASSPHRASE_PROPERTY_NAME)) {
                    // Remove the property
                    properties.remove(index);

                    // If the values equal
                    if (property.getValue().equals(passphrase)) {
                        authenticated = true;
                        break;
                    }

                    // Return
                    return new HandshakeAuthenticationResult(replacedHost, uuid, AuthenticationResult.HANDSHAKE_INVALID_PASSPHRASE);
                }
            }

            // Property not found
            if (!authenticated)
                return new HandshakeAuthenticationResult(replacedHost, uuid, AuthenticationResult.HANDSHAKE_PROPERTY_NOT_FOUND);

            // Add verification property
            properties.add(new Property(SESSION_PROPERTY_NAME, sessionKey, ""));
            // JSON
            String json = GSON.toJson(properties);

            // Start and end
            String start = join(data, 0, propertiesIndex), end = join(data, propertiesIndex + 1, data.length);
            // Append delimiters
            if (start.length() > 0)
                start += HOST_DELIMITER;
            if (end.length() > 0)
                end = HOST_DELIMITER + end;

            // Return
            return new HandshakeAuthenticationResult(start + json + end, uuid, AuthenticationResult.SUCCESS);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred during handshake authentication!", ex);
            return new HandshakeAuthenticationResult(replacedHost, uuid, AuthenticationResult.UNKNOWN_ERROR);
        }
    }

    /**
     * Authenticates the given player's session.
     *
     * @param player the player to authenticate
     * @return the result
     */
    public AuthenticationResult session(@NotNull Player player) {
        // Check fields
        if (profileMethod == null || craftPlayerClass == null)
            return AuthenticationResult.SESSION_REFLECTION_UNAVAILABLE;

        try {
            // Profile
            GameProfile profile = (GameProfile) profileMethod.invoke(craftPlayerClass.cast(player));
            if (profile == null)
                return AuthenticationResult.SESSION_NO_GAME_PROFILE;

            // Properties
            PropertyMap propertyMap = profile.getProperties();
            if (propertyMap == null || propertyMap.size() == 0)
                return AuthenticationResult.SESSION_NO_PROPERTIES;

            // Exactly one property required
            Collection<com.mojang.authlib.properties.Property> properties = propertyMap.get(SESSION_PROPERTY_NAME);
            if (properties == null || properties.size() == 0)
                return AuthenticationResult.SESSION_PROPERTY_NOT_FOUND;

            // Delete possible properties with the passphrase
            propertyMap.removeAll(PassphraseStore.PASSPHRASE_PROPERTY_NAME);

            // If there are more entries
            if (properties.size() != 1)
                return AuthenticationResult.SESSION_UNEXPECTED_PROPERTIES;

            // Property
            com.mojang.authlib.properties.Property property = properties.iterator().next();
            // Is this needed?
            if (property.getName() == null || !property.getName().equals(SESSION_PROPERTY_NAME))
                return AuthenticationResult.SESSION_PROPERTY_NOT_FOUND;

            // Compare keys
            if (property.getValue() == null || !property.getValue().equals(sessionKey))
                return AuthenticationResult.SESSION_INVALID;

            // Past this point we don't really care about the exposure of the session key, we delete it just to save bandwidth
            propertyMap.removeAll(SESSION_PROPERTY_NAME);

            // Passed
            return AuthenticationResult.SUCCESS;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred during session authentication!", ex);
            return AuthenticationResult.UNKNOWN_ERROR;
        }
    }

    /**
     * Joins all elements within the given indexing range into a string, separated by {@link #HOST_DELIMITER}.
     *
     * @param array array to join
     * @param start first index (inclusive)
     * @param end   last index (exclusive)
     * @return the joined elements
     */
    private String join(String[] array, int start, int end) {
        // Builder
        StringBuilder builder = new StringBuilder();
        // Append
        for (; start < end; start++) {
            // Append a space
            if (builder.length() != 0)
                builder.append(HOST_DELIMITER);
            // Append
            builder.append(array[start]);
        }

        // Return
        return builder.toString();
    }

}