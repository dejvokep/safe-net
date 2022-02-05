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
package dev.dejvokep.securednetwork.core.authenticator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authenticator covering all needed functions related to passphrase.
 */
public class Authenticator {

    /**
     * Property name.
     */
    public static final String PROPERTY_NAME = "secured_network";
    /**
     * String used as a splitter for the hostname value.
     */
    private static final String HOST_SPLIT_REGEX = "\00";
    /**
     * Start of the JSON containing all the properties.
     */
    private static final String PROPERTIES_START = "[{\"";
    /**
     * String found in the hostname split if the player connected through Geyser. Surrounded by a string from both sides
     * (in the hostname split).
     */
    private static final String GEYSER_FLOODGATE_ID = "Geyser-Floodgate";

    /**
     * Replacement for unknown data.
     */
    public static final String UNKNOWN_DATA = "<unknown>";

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

    /**
     * Passphrase characters (90 chars) used to generate the passphrase.
     */
    private static final String PASSPHRASE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-=[];,./~!@#$%^&*()_+{}|:<>?";

    // Passphrase
    private String passphrase;

    // The config
    private final YamlDocument config;
    // The logger
    private final Logger logger;

    /**
     * Calls {@link #reload()} to load the internal data.
     *
     * @param config          the configuration file
     * @param logger    the logger
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
            return new AuthenticationRequest(UNKNOWN_DATA, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_MALFORMED_DATA);
        // No passphrase configured
        if (passphrase == null || passphrase.length() == 0)
            return new AuthenticationRequest(host, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_PASSPHRASE_NOT_CONFIGURED);

        // Replaced host
        String replacedHost = host.replace(this.passphrase, "<passphrase>");
        // Split the host value
        String[] data = host.split(HOST_SPLIT_REGEX);

        // If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (data.length < 3 || data.length > 7)
            return new AuthenticationRequest(replacedHost, UNKNOWN_DATA, AuthenticationRequest.Result.FAILED_INSUFFICIENT_LENGTH);

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
                return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_NO_PROPERTIES);
        } catch (Exception ignored) {
            return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_NO_PROPERTIES);
        }

        try {
            // The property
            Property property;

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
                        return new AuthenticationRequest(host.replace(data[propertiesIndex], GSON.toJson(properties)), uuid, AuthenticationRequest.Result.PASSED);
                    else
                        break;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An unknown error occurred while processing player's connection!", ex);
            return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_UNKNOWN_ERROR);
        }

        // Return
        return new AuthenticationRequest(replacedHost, uuid, AuthenticationRequest.Result.FAILED_PASSPHRASE_NOT_FOUND);
    }

    /**
     * Generates a new passphrase of the specified length and it into the configuration file.
     *
     * @param length the desired length of the new passphrase (<code>length > 0</code>)
     */
    public void generatePassphrase(int length) throws IOException {
        // If the length is less than 1
        if (length < 1)
            return;

        // Secure random
        SecureRandom random = new SecureRandom();
        // String builder
        StringBuilder stringBuilder = new StringBuilder();
        // Build the passphrase
        for (int count = 0; count < length; count++)
            // Append a new character
            stringBuilder.append(PASSPHRASE_CHARS.charAt(random.nextInt(PASSPHRASE_CHARS.length())));
        // Set into the config
        config.set("passphrase", stringBuilder.toString());
        // Save
        config.save();
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