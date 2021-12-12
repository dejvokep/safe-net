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
package dev.dejvokep.securednetwork.core.authenticator;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dev.dejvokep.securednetwork.core.config.Config;
import dev.dejvokep.securednetwork.core.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;

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
    private final Config config;
    // The log
    private final Log log;

    /**
     * Calls {@link #reload()} to load the internal data.
     *
     * @param config the configuration file
     * @param log    the logger
     */
    public Authenticator(@NotNull Config config, @NotNull Log log) {
        // Set
        this.config = config;
        this.log = log;
        // Reload
        reload();
    }

    /**
     * Authenticates a player by the given host string obtained from the handshake packet.
     *
     * @param host the host string obtained from the handshake packet
     * @return an authentication result
     */
    public AuthenticationResult authenticate(@NotNull String host) {
        // Split the host value
        String[] data = host.split(HOST_SPLIT_REGEX);

        // If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (data.length < 3 || data.length > 7) {
            // Log the result
            logResult("?", false, "insufficient_length");
            // Return
            return new AuthenticationResult(host.replace(this.passphrase, ""), false, "?");
        }

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
            // Parse
            properties = GSON.fromJson(data[propertiesIndex], PROPERTY_LIST_TYPE);
        } catch (JsonSyntaxException | ArrayIndexOutOfBoundsException ignored) {
            // Log the result
            logResult(uuid, false, "no_property");
            // Return
            return new AuthenticationResult(host.replace(this.passphrase, ""), false, uuid);
        }

        try {
            // The property
            Property property;

            // Loop through all properties
            for (int index = properties.size() - 1; index >= 0; index--) {
                // Get the property
                property = properties.get(index);

                // If the names equal
                if (property.getName().equals(PROPERTY_NAME)) {
                    // Remove the property
                    properties.remove(index);

                    // If the values equal
                    if (property.getValue().equals(this.passphrase)) {
                        // Log the result
                        logResult(uuid, true, null);
                        // Return and replace the passphrase just in case
                        return new AuthenticationResult(host.replace(data[propertiesIndex], GSON.toJson(properties)), true, uuid);
                    } else {
                        // Break
                        break;
                    }
                }
            }
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {
        }

        // Log the result
        logResult(uuid, false, "incorrect_property");
        // Return
        return new AuthenticationResult(host.replace(this.passphrase, ""), false, uuid);
    }

    /**
     * Generates a new passphrase of the specified length and it into the configuration file.
     *
     * @param length the desired length of the new passphrase (<code>length > 0</code>)
     */
    public void generatePassphrase(int length) {
        // If the length is less than 1
        if (length < 1)
            return;

        // Generating
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "Generating a new passphrase of length " + length + ".");

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

        // Generated
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "The new passphrase has been generated successfully.");
    }

    /**
     * Logs the result of an authentication request.
     *
     * @param playerId UUID representing the player who invoked the request (or <code>?</code> if unknown)
     * @param passed   if the player authenticated successfully
     * @param cause    the cause (if the authentication failed, otherwise <code>null</code>)
     */
    private void logResult(@Nullable String playerId, boolean passed, @Nullable String cause) {
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "uuid=" + playerId + " result=" + (passed ? "passed" : "failed") +
                (cause != null ? ", cause=" + cause : ""));
    }

    /**
     * Reloads the passphrase.
     */
    public void reload() {
        // Passphrase
        passphrase = config.getString("passphrase");
        // Log the warning
        if (passphrase.length() == 0)
            log.logConsole(Level.SEVERE, Log.Source.AUTHENTICATOR, "No passphrase configured (length is 0)! The plugin will disconnect all incoming connections. Please generate one as soon as possible from the proxy console with \"/sn generate\".");
        else if (passphrase.length() < WEAK_PASSPHRASE_LENGTH_THRESHOLD)
            log.logConsole(Level.SEVERE, Log.Source.AUTHENTICATOR, "The configured passphrase is weak! It should be at least " + WEAK_PASSPHRASE_LENGTH_THRESHOLD +
                    " characters long; though the recommended length is " + RECOMMENDED_PASSPHRASE_LENGTH + ". Please generate one as soon as possible from the proxy console with \"/sn generate\".");
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