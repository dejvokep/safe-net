package com.davidcubesvk.securedNetworkBackend.config;

import com.davidcubesvk.securedNetworkCore.config.Config;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Configuration file representation used if on a backend server, implements the {@link Config} interface.
 */
public class ConfigBackend implements Config {

    //File and configuration representations
    private final File file;
    private final YamlConfiguration config = new YamlConfiguration();

    /**
     * Creates a config file out of a resource file by their names and loads it.
     *
     * @param plugin       the plugin instance
     * @param resourceName the resource file name in project source
     * @param fileName     a name for the file to create
     */
    public ConfigBackend(Plugin plugin, String resourceName, String fileName) {
        //Create the folder(s)
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        //Create an abstract instance of the file
        file = new File(plugin.getDataFolder(), fileName);
        //If the file doesn't exist
        if (!file.exists()) {
            try (InputStream in = plugin.getResource(resourceName)) {
                Files.copy(in, file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //Load
        load();
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public int getInt(String path) {
        return config.getInt(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, value);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Object getConfig() {
        return config;
    }

    @Override
    public void load() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
