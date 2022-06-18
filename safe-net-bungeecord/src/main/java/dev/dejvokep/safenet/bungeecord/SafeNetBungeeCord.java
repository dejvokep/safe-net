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
package dev.dejvokep.safenet.bungeecord;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.safenet.bungeecord.command.PluginCommand;
import dev.dejvokep.safenet.bungeecord.ipwhitelist.AddressWhitelist;
import dev.dejvokep.safenet.bungeecord.listener.LoginListener;
import dev.dejvokep.safenet.bungeecord.message.Messenger;
import dev.dejvokep.safenet.bungeecord.updater.Updater;
import dev.dejvokep.safenet.core.authenticator.Authenticator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Main class for the proxy-side of the plugin.
 */
public class SafeNetBungeeCord extends Plugin {

    // Plugin
    private Plugin plugin;

    // Message sender
    private final Messenger messenger = new Messenger();
    // Config
    private YamlDocument config;
    // Authenticator
    private Authenticator authenticator;
    // Address whitelist
    private AddressWhitelist addressWhitelist;
    // Login listener
    private LoginListener listener;

    @Override
    public void onEnable() {
        // Set the plugin instance
        plugin = this;
        // Thank you message
        getLogger().info("Thank you for downloading SafeNET!");

        try {
            // Create the config file
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResourceAsStream("bungee-config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize the config file! Shutting down...", ex);
            ProxyServer.getInstance().stop();
            return;
        }

        // Initialize
        authenticator = new Authenticator(config, getLogger());

        // Initialize
        addressWhitelist = new AddressWhitelist(this);

        // Plugin manager
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        // Register listener
        pluginManager.registerListener(this, listener = new LoginListener(this));
        // Register commands
        pluginManager.registerCommand(this, new PluginCommand(this, "safenet"));
        pluginManager.registerCommand(this, new PluginCommand(this, "sn"));

        // Initialize
        Updater.watch(this);

        // If enabled
        if (config.getBoolean("metrics")) {
            // Initializing metrics
            getLogger().info("Initializing metrics.");
            // Initialize Metrics
            new Metrics(this, 6479);
        }

    }

    /**
     * Returns the plugin instance.
     *
     * @return the plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the configuration file representation.
     *
     * @return the configuration file.
     */
    public YamlDocument getConfiguration() {
        return config;
    }

    /**
     * Returns the authenticator.
     *
     * @return the authenticator
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Returns the messenger.
     *
     * @return the messenger
     */
    public Messenger getMessenger() {
        return messenger;
    }

    /**
     * Returns the address whitelist.
     *
     * @return the address whitelist
     */
    public AddressWhitelist getAddressWhitelist() {
        return addressWhitelist;
    }

    /**
     * Returns the login event listener.
     *
     * @return the login event listener
     */
    public LoginListener getListener() {
        return listener;
    }

}