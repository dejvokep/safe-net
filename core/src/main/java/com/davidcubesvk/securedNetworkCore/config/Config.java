package com.davidcubesvk.securedNetworkCore.config;

import java.io.File;
import java.util.List;

/**
 * Interface representation of a configuration file. Includes basic configuration methods and adds an option to
 * retrieve the original configuration object, or the file representation.
 */
public interface Config {

    /**
     * Returns a string located at the specified path.
     *
     * @param path the path to locate
     * @return the string at the path
     */
    String getString(String path);

    /**
     * Returns an integer located at the specified path.
     *
     * @param path the path to locate
     * @return the integer at the path
     */
    int getInt(String path);

    /**
     * Returns a boolean located at the specified path.
     *
     * @param path the path to locate
     * @return the boolean at the path
     */
    boolean getBoolean(String path);

    /**
     * Returns a list of strings located at the specified path.
     *
     * @param path the path to locate
     * @return the list of strings at the path
     */
    List<String> getStringList(String path);

    /**
     * Returns an object located at the specified path.
     * @param path the path to locate
     * @return the object at the path
     */
    Object get(String path);

    /**
     * Sets a value to the specified path.
     *
     * @param path  the path to set the value to
     * @param value the value to set
     */
    void set(String path, Object value);

    /**
     * Returns the file instance of the config.
     *
     * @return the file instance of the config
     */
    File getFile();

    /**
     * Returns the native config representation.
     *
     * @return the native config representation
     */
    Object getConfig();

    /**
     * (Re)loads the config.
     */
    void load();

    /**
     * Saves the config.
     */
    void save();

}