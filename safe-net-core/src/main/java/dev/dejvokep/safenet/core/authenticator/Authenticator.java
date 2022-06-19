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
package dev.dejvokep.safenet.core.authenticator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticator covering all needed functions related to passphrase.
 */
public class Authenticator {

    /**
     * Property name.
     */
    public static final String PROPERTY_NAME = "safe_net";
    /**
     * Host data delimiter.
     */
    private static final String HOST_DELIMITER = "\00";
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
     * Pattern which matches the client's textures property.
     */
    public static final Pattern TEXTURES_PATTERN = Pattern.compile("\\{\"name\":\"textures\",\"value\":\"[^\"]+\",\"signature\":\"[^\"]+\"}");

    /**
     * Shortened textures, used as a replacement for {@link #TEXTURES_PATTERN}.
     */
    public static final String TEXTURES_SHORTENED = Matcher.quoteReplacement("{\"name\":\"textures\",\"value\":\"<value>\",\"signature\":\"<signature>\"}");

    /**
     * Recommended passphrase length.
     */
    public static final int RECOMMENDED_PASSPHRASE_LENGTH = 1000;

    /**
     * Passphrases with lengths below this threshold are considered weak and should be reset immediately.
     */
    public static final int WEAK_PASSPHRASE_LENGTH_THRESHOLD = 50;

    /**
     * GSON instance.
     */
    private static final Gson GSON = new Gson();
    /**
     * Property list type used to parse the properties JSON.
     */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<ArrayList<Property>>() {
    }.getType();

    // Passphrase
    private String passphrase;

    // The config
    private final YamlDocument config;
    // The logger
    private final Logger logger;

    /**
     * Calls {@link #reload()} to load the internal data.
     *
     * @param config the configuration file
     * @param logger the logger
     */
    public Authenticator(@NotNull YamlDocument config, @NotNull Logger logger) {
        // Set
        this.config = config;
        this.logger = logger;
        // Reload
        reload();
    }

    /**
     * Authenticates a player by the given host string obtained from the handshake packet.
     *
     * @param host the host string obtained from the handshake packet
     * @return an authentication result
     */
    public AuthenticationRequest authenticate(@Nullable String host) {
        // If null
        if (host == null)
            return new AuthenticationRequest(UNKNOWN_DATA, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_MALFORMED_DATA, UNKNOWN_DATA);
        // No passphrase configured
        if (passphrase == null || passphrase.length() == 0)
            return new AuthenticationRequest(host, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_PASSPHRASE_NOT_CONFIGURED, UNKNOWN_DATA);

        // Replaced host
        String replacedHost = host.replace(this.passphrase, "<passphrase>");
        // Split the host value
        String[] data = host.split(HOST_DELIMITER);

        // If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (data.length < 3 || data.length > 7)
            return new AuthenticationRequest(replacedHost, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_INSUFFICIENT_LENGTH, replacedHost);

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
                return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_NO_PROPERTIES, replacedHost);
        } catch (Exception ignored) {
            return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_NO_PROPERTIES, replacedHost);
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
                if (property.getName().equals(PROPERTY_NAME)) {
                    // Remove the property
                    properties.remove(index);

                    // If the values equal
                    if (property.getValue().equals(this.passphrase))
                        authenticated = true;
                    break;
                }
            }

            // If authenticated
            if (authenticated) {
                // JSON
                String json = GSON.toJson(properties);
                // Shortened
                String shortenedJson = TEXTURES_PATTERN.matcher(json).replaceFirst(TEXTURES_SHORTENED);

                // Start and end
                String start = join(data, 0, propertiesIndex), end = join(data, propertiesIndex + 1, data.length);
                // Append delimiters
                if (start.length() > 0)
                    start += HOST_DELIMITER;
                if (end.length() > 0)
                    end = HOST_DELIMITER + end;

                // Return
                return new AuthenticationRequest(start + json + end, uuid, AuthenticationRequest.Result.PASSED, start + shortenedJson + end);
            } else {
                // Return
                return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_PASSPHRASE_NOT_FOUND, replacedHost);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An unknown error occurred while processing player's connection!", ex);
            return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_UNKNOWN_ERROR, replacedHost);
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

    /**
     * Generates a new passphrase of the specified length and sets it into the configuration file.
     *
     * @param length the desired length of the new passphrase (<code>length > 0</code>)
     */
    public final void generatePassphrase(int length) throws IOException {
        // If the length is less than 1
        if (length < 1)
            return;

        // Set into the config
        config.set("passphrase", KeyGenerator.generate(length));
        // Save
        config.save();
    }

    /**
     * Returns the current status of the passphrase.
     * <ul>
     *     <li><code>length == 0</code>: invalid</li>
     *     <li><code>length < {@link #WEAK_PASSPHRASE_LENGTH_THRESHOLD}</code>: weak</li>
     *     <li><code>length >= {@link #WEAK_PASSPHRASE_LENGTH_THRESHOLD}</code>: valid</li>
     * </ul>
     *
     * @return the passphrase status
     */
    public String getPassphraseStatus() {
        return passphrase.length() == 0 ? "invalid" : passphrase.length() < WEAK_PASSPHRASE_LENGTH_THRESHOLD ? "weak" : "valid";
    }

    /**
     * Reloads the passphrase.
     */
    public void reload() {
        // Passphrase
        passphrase = config.getString("passphrase");

        // Log the warning
        if (passphrase.length() == 0)
            logger.severe("No passphrase configured (length is 0)! The plugin will disconnect all incoming connections. Please generate one as soon as possible from the proxy console with \"/sn generate\".");
        else if (passphrase.length() < WEAK_PASSPHRASE_LENGTH_THRESHOLD)
            logger.severe("The configured passphrase is weak! It should be at least " + WEAK_PASSPHRASE_LENGTH_THRESHOLD + " characters long; though the recommended length is " + RECOMMENDED_PASSPHRASE_LENGTH + ". Please generate one as soon as possible from the proxy console with \"/sn generate\".");
    }

    /**
     * Returns the passphrase.
     *
     * @return the passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

}