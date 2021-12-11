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
package dev.dejvokep.securednetwork.bungeecord.util.config;

import dev.dejvokep.securednetwork.core.config.Config;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Configuration file representation used if on a proxy server, implements the {@link Config} interface.
 */
public class ConfigProxy implements Config {

    // File and configuration representations
    private final File file;
    private Configuration config;

    /**
     * Creates a config file out of a resource file by their names and loads it.
     *
     * @param plugin       the plugin instance
     * @param resourceName the resource file name in project source
     * @param fileName     a name for the file to create
     */
    public ConfigProxy(@NotNull Plugin plugin, @NotNull String resourceName, @NotNull String fileName) {
        // Create the folder
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        // Create an abstract instance of the file
        file = new File(plugin.getDataFolder(), fileName);
        // If doesn't exist
        if (!file.exists()) {
            // Copy the configuration
            try (InputStream in = plugin.getResourceAsStream(resourceName)) {
                Files.copy(in, file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Load
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
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
