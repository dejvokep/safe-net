package com.davidcubesvk.securedNetworkCore.authenticator;

import com.davidcubesvk.securedNetworkCore.config.Config;
import com.davidcubesvk.securedNetworkCore.log.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Authenticator covering all needed functions related to passphrase.<br>
 * <p></p>
 * Credit for some parts of this class goes to project BungeeGuard (https://github.com/lucko/BungeeGuard) and
 * it's contributors.
 */
public class Authenticator {

    /**
     * Default property name.
     */
    private static final String DEFAULT_PROPERTY_NAME = "secured_network";
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
     * GSON instance.
     */
    private static final Gson GSON = new Gson();
    /**
     * Property list type used to parse the properties JSON.
     */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<ArrayList<Property>>() {
    }.getType();

    /**
     * Passphrase characters (85 chars) used to generate the passphrase.
     */
    private static final String PASSPHRASE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@$%^&*()_+-=_+[];,.<>?";

    //Property name and passphrase
    private String propertyName, passphrase;

    //The config
    private final Config config;
    //The log
    private final Log log;

    /**
     * Calls {@link #reload()} to load the internal data.
     *
     * @param config the configuration file
     * @param log    the logger
     */
    public Authenticator(Config config, Log log) {
        //Set
        this.config = config;
        this.log = log;
        //Reload
        reload();
    }

    /**
     * Authenticates a player by the given host string obtained from the handshake packet.
     *
     * @param host the host string obtained from the handshake packet
     * @return an authentication result
     */
    public AuthenticationResult authenticate(String host) {
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, host);
        //Split the host value
        String[] data = host.split(HOST_SPLIT_REGEX);

        //If the length is less than 3 or greater than 7 (GeyserMC compatibility)
        if (data.length < 3 || data.length > 7) {
            //Log the result
            logResult("?", false, "insufficient_length");
            //Return
            return new AuthenticationResult(host.replace(this.passphrase, ""), false, "?");
        }

        //The player's UUID
        String uuid = data.length <= 4 ? data[2] : null;
        //The properties index
        int propertiesIndex = -1;

        //Go through all indexes (excluding 0, as there can not be anything useful)
        for (int i = 1; i < data.length; i++) {
            //If it is the Geyser Floodgate ID string
            if (data[i].equals(GEYSER_FLOODGATE_ID))
                //Skip the next index
                i++;
            else if (data[i].startsWith(PROPERTIES_START))
                //Set the properties index
                propertiesIndex = i;
            else if (uuid == null && data[i].length() == 32)
                //If is the UUID (length is 32)
                uuid = data[i];
        }

        //Properties
        ArrayList<Property> properties;
        //Parse properties from the last index
        try {
            //Parse
            properties = GSON.fromJson(data[propertiesIndex], PROPERTY_LIST_TYPE);
        } catch (JsonSyntaxException | ArrayIndexOutOfBoundsException ignored) {
            //Log the result
            logResult(uuid, false, "no_property");
            //Return
            return new AuthenticationResult(host.replace(this.passphrase, ""), false, uuid);
        }

        try {
            //The property
            Property property;

            //Loop through all properties
            for (int index = properties.size() - 1; index >= 0; index--) {
                //Get the property
                property = properties.get(index);

                //If the names equal
                if (property.getName().equals(this.propertyName)) {
                    //Remove the property
                    properties.remove(index);

                    //If the values equal
                    if (property.getValue().equals(this.passphrase)) {
                        //Log the result
                        logResult(uuid, true, null);
                        //Return and replace the passphrase just in case
                        return new AuthenticationResult(host.replace(data[propertiesIndex], GSON.toJson(properties)), true, uuid);
                    } else {
                        //Break
                        break;
                    }
                }
            }
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {
        }

        //Log the result
        logResult(uuid, false, "incorrect_property");
        //Return
        return new AuthenticationResult(host.replace(this.passphrase, ""), false, uuid);
    }

    /**
     * Generates a new passphrase of the specified length and it into the configuration file.
     *
     * @param length the desired length of the new passphrase (<code>length > 0</code>)
     */
    public void generatePassphrase(int length) {
        //If the length is less than 1
        if (length < 1)
            return;

        //Generating
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "Generating a new passphrase of length " + length + ".");

        //Secure random
        SecureRandom random = new SecureRandom();
        //String builder
        StringBuilder stringBuilder = new StringBuilder();
        //Build the passphrase
        for (int count = 0; count < length; count++)
            //Append a new character
            stringBuilder.append(PASSPHRASE_CHARS.charAt(random.nextInt(PASSPHRASE_CHARS.length())));
        //Set into the config
        config.set("property.value", stringBuilder.toString());
        //Save
        config.save();

        //Generated
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "The new passphrase has been generated successfully.");
    }

    /**
     * Logs the result of an authentication request.
     *
     * @param playerId UUID representing the player who invoked the request (or <code>?</code> if unknown)
     * @param passed   if the player authenticated successfully
     * @param cause    the cause (if the authentication failed, otherwise <code>null</code>)
     */
    private void logResult(String playerId, boolean passed, String cause) {
        log.log(Level.INFO, Log.Source.AUTHENTICATOR, "uuid=" + playerId + " result=" + (passed ? "passed" : "failed") +
                (cause != null ? ", cause=" + cause : ""));
    }

    /**
     * Reloads the internal data.
     */
    public void reload() {
        //Property name
        propertyName = config.getString("property.name");

        //If the name is invalid
        if (propertyName.equals("textures")) {
            //Set to default
            propertyName = DEFAULT_PROPERTY_NAME;
            //Log
            log.logConsole(Level.SEVERE, Log.Source.AUTHENTICATOR, "Invalid property name! Using default \"" + DEFAULT_PROPERTY_NAME + "\" name.");
        }

        //Passphrase
        passphrase = config.getString("property.value");
    }

    /**
     * Returns the name of the property used by the plugin.
     *
     * @return the name of the property used by the plugin
     */
    public String getPropertyName() {
        return propertyName;
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