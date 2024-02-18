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
package dev.dejvokep.safenet.spigot.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import dev.dejvokep.safenet.core.KeyGenerator;
import dev.dejvokep.safenet.core.PassphraseVault;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A class used to authenticate handshakes and sessions (to protect against uncaught handshakes during startup).
 */
public class Authenticator {

    /**
     * Default property name for session storage.
     */
    public static final String DEFAULT_SESSION_PROPERTY_NAME = "safe_net_session";
    /**
     * Length of the generated session key. There is only one key generated per server cycle (start - stop), unless the
     * plugin is reloaded, what is strictly prohibited (makes the server vulnerable).
     */
    public static final int SESSION_KEY_LENGTH = 1000;

    /**
     * Host data delimiter.
     */
    private static final String HOST_DELIMITER = "\00";
    /**
     * Element found in the hostname split if the player connected through GeyserMC server. Indicates that the next
     * element is a chunk of Floodgate related data. <b>Applies to Floodgate V1 specification.</b>
     */
    private static final String GEYSER_FLOODGATE_ID_V1 = "Geyser-Floodgate";
    /**
     * String prefixing Floodgate related data in an element in the hostname split, if the player connected through
     * GeyserMC server. <b>Applies to Floodgate V2 specification.</b>
     */
    private static final String GEYSER_FLOODGATE_ID_V2 = "^Floodgate^";

    /**
     * Replacement for unknown data.
     */
    public static final String UNKNOWN_DATA = "<unknown>";

    /**
     * Pattern used to validate socket address hostname.
     */
    public static final Pattern SOCKET_ADDRESS_HOSTNAME_PATTERN = Pattern.compile("[0-9a-f.:]{0,45}");
    /**
     * Pattern for matching and converting to UUID with dashes.
     */
    private static final Pattern UUID_DASH_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    /**
     * GSON instance.
     */
    private static final Gson GSON = new Gson();
    /**
     * Property list type used to parse the properties JSON.
     */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<ArrayList<Property>>() {
    }.getType();

    // Plugin
    private final SafeNetSpigot plugin;

    // Session key used to protect against uncaught handshakes and property name
    private final String sessionKey = KeyGenerator.generate(SESSION_KEY_LENGTH);
    private String sessionPropertyName;
    // Class and method necessary for profile manipulation
    private Class<?> craftPlayerClass = null;
    private Method profileMethod = null;

    /**
     * Initializes the authenticator.
     *
     * @param plugin the plugin
     */
    public Authenticator(@NotNull SafeNetSpigot plugin) {
        // Set
        this.plugin = plugin;

        try {
            craftPlayerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
            profileMethod = craftPlayerClass.getDeclaredMethod("getProfile");
            profileMethod.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst utilizing server classes!", ex);
        }

        reload();
    }

    /**
     * Authenticates handshake by the given host string obtained from the handshake packet.
     *
     * @param data the host string
     * @return the result
     */
    public HandshakeAuthenticationResult handshake(@Nullable String data) {
        // Passphrase
        String passphrase = plugin.getPassphraseVault().getPassphrase();
        // If null
        if (data == null)
            return new HandshakeAuthenticationResult(AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
        // No passphrase configured
        if (passphrase == null || passphrase.length() == 0)
            return new HandshakeAuthenticationResult(data, AuthenticationResult.HANDSHAKE_PASSPHRASE_NOT_CONFIGURED);

        // Replaced host
        String replaced = data.replace(passphrase, "<passphrase>");
        // Split the host value
        String[] split = data.split(HOST_DELIMITER);

        // If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (split.length < 3 || split.length > 7)
            return new HandshakeAuthenticationResult(replaced, AuthenticationResult.HANDSHAKE_INSUFFICIENT_DATA_LENGTH);

        // Properties index
        int propertiesIndex = -1;
        // Data
        String serverHostname = null, socketAddressHostname = null;
        UUID uuid = null;

        // Go through all indexes
        for (int i = 0; i < split.length; i++) {
            // Floodgate V1 ID checking
            if (split[i].equals(GEYSER_FLOODGATE_ID_V1)) {
                // Skip the next index
                i++;
                continue;
            }
            // Floodgate V2 ID checking
            if (split[i].startsWith(GEYSER_FLOODGATE_ID_V2))
                continue;

            // Set
            if (serverHostname == null) {
                serverHostname = split[i];
            } else if (socketAddressHostname == null) {
                socketAddressHostname = split[i];
            } else if (uuid == null) {
                try {
                    uuid = UUID.fromString(split[i].contains("-") ? split[i] : UUID_DASH_PATTERN.matcher(split[i]).replaceAll("$1-$2-$3-$4-$5"));
                } catch (IllegalArgumentException ex) {
                    return new HandshakeAuthenticationResult(replaced, AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
                }
            } else if (propertiesIndex == -1) {
                propertiesIndex = i;
            }
        }

        // No hostname, socket address and uuid, or they are invalid
        if (uuid == null || !SOCKET_ADDRESS_HOSTNAME_PATTERN.matcher(socketAddressHostname).matches())
            return new HandshakeAuthenticationResult(replaced, AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
        // No properties
        if (propertiesIndex == -1)
            return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_NO_PROPERTIES);

        // Properties
        ArrayList<Property> properties;
        // Parse properties from the last index
        try {
            properties = GSON.fromJson(split[propertiesIndex], PROPERTY_LIST_TYPE);
            //If null
            if (properties == null)
                return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_NO_PROPERTIES);
        } catch (JsonSyntaxException ignored) {
            return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_MALFORMED_DATA);
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
                if (property.getName().equals(plugin.getPassphraseVault().getPropertyName())) {
                    // Remove the property
                    properties.remove(index);

                    // If the values equal
                    if (property.getValue().equals(passphrase)) {
                        authenticated = true;
                        break;
                    }

                    // Return
                    return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_INVALID_PASSPHRASE);
                }
            }

            // Property not found
            if (!authenticated)
                return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.HANDSHAKE_PROPERTY_NOT_FOUND);

            // Add verification property
            if (!plugin.isPaperServer())
                properties.add(new Property(sessionPropertyName, sessionKey, ""));
            // JSON
            String json = GSON.toJson(properties);

            // Start and end
            String start = join(split, 0, propertiesIndex), end = join(split, propertiesIndex + 1, split.length);
            // Append delimiters
            if (start.length() > 0)
                start += HOST_DELIMITER;
            if (end.length() > 0)
                end = HOST_DELIMITER + end;

            // Return
            return new HandshakeAuthenticationResult(start + json + end, serverHostname, socketAddressHostname, uuid, json, AuthenticationResult.SUCCESS);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred during handshake authentication!", ex);
            return new HandshakeAuthenticationResult(replaced, serverHostname, socketAddressHostname, uuid, UNKNOWN_DATA, AuthenticationResult.UNKNOWN_ERROR);
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
            Collection<com.mojang.authlib.properties.Property> properties = propertyMap.get(sessionPropertyName);
            if (properties.size() == 0)
                return AuthenticationResult.SESSION_PROPERTY_NOT_FOUND;

            // Delete possible properties with the passphrase
            propertyMap.removeAll(plugin.getPassphraseVault().getPropertyName());

            // If there are more entries
            if (properties.size() != 1)
                return AuthenticationResult.SESSION_UNEXPECTED_PROPERTIES;

            // Property
            com.mojang.authlib.properties.Property property = properties.iterator().next();
            // Is this needed?
            if (property.getName() == null || !property.getName().equals(sessionPropertyName))
                return AuthenticationResult.SESSION_PROPERTY_NOT_FOUND;

            // Compare keys
            if (property.getValue() == null || !property.getValue().equals(sessionKey))
                return AuthenticationResult.SESSION_INVALID;

            // Past this point we don't really care about the exposure of the session key
            propertyMap.removeAll(sessionPropertyName);

            // Passed
            return AuthenticationResult.SUCCESS;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred during session authentication!", ex);
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
    private String join(@NotNull String[] array, int start, int end) {
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

    /**
     * Reloads the session components.
     */
    public void reload() {
        this.sessionPropertyName = plugin.getConfiguration().getString("property-name.session", DEFAULT_SESSION_PROPERTY_NAME);
    }

}