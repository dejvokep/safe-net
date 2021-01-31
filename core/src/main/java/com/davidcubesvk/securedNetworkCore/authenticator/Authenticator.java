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
 * To parse the properties JSON, the GSON library is used. The benefit of using it is that it is already provided by the
 * server - it does not have to be compiled within this plugin.
 */
public class Authenticator {

    //Default property name
    public static final String DEFAULT_PROPERTY_NAME = "secured_network";
    //String used as a splitter for the host value
    public static final String SPLIT_STRING = "\00";

    //Gson instance
    public static final Gson GSON = new Gson();
    //Property list type
    public static final Type PROPERTY_LIST_TYPE = new TypeToken<ArrayList<Property>>() {
    }.getType();

    //Passphrase characters
    public static final String PASSPHRASE_CHARS = "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789!@#$%^&*()_+-=_+[];,.<>?";

    //Property name and passphrase
    private String propertyName, passphrase;

    private Config config;
    private Log log;

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
        //Split the host value
        String[] data = host.split(SPLIT_STRING);

        //If the length is not 3 or 4
        if (data.length != 3 && data.length != 4) {
            //Log the result
            logResult("?", false, "insufficient_length");
            //Return
            return new AuthenticationResult(data.length > 1 ? data[0] : "", false, "?");
        }

        //The player's UUID
        String uuid = data[2];

        //Properties
        ArrayList<Property> properties = null;
        //Parse properties from the last index
        try {
            //Parse
            properties = GSON.fromJson(data[3], PROPERTY_LIST_TYPE);
        } catch (JsonSyntaxException | ArrayIndexOutOfBoundsException ignored) {
        }

        //If the property array is invalid
        if (properties == null) {
            //Log the result
            logResult(uuid, false, "no_property");
            //Return
            return new AuthenticationResult(data[0], false, uuid);
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
                        return new AuthenticationResult(data[0] + SPLIT_STRING + data[1] + SPLIT_STRING + data[2] + SPLIT_STRING +
                                GSON.toJson(properties).replace("\"" + this.passphrase + "\"", "\"\""), true, uuid);
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
        return new AuthenticationResult(data[0], false, uuid);
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