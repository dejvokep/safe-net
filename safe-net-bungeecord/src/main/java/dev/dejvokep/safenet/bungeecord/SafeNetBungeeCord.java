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
import dev.dejvokep.safenet.core.PassphraseVault;
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

    /**
     * Supported version of the BungeeCord server.
     */
    private static final String BUNGEECORD_VERSION = "build 1637 (1.19 update) or newer";

    // Message sender
    private final Messenger messenger = new Messenger();
    // Config
    private YamlDocument config;
    // Passphrase vault
    private PassphraseVault passphraseVault;
    // Address whitelist
    private AddressWhitelist addressWhitelist;
    // Login listener
    private LoginListener listener;

    @Override
    public void onEnable() {
        // If BungeeCord is not of a support version
        if (isUnsupportedBungeeCord()) {
            getLogger().severe(String.format("This version of SafeNET requires BungeeCord %s to run! Shutting down...", BUNGEECORD_VERSION));
            ProxyServer.getInstance().stop();
            return;
        }

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
        passphraseVault = new PassphraseVault(config, getLogger());
        addressWhitelist = new AddressWhitelist(this);
        Updater.watch(this);

        // Plugin manager
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        // Register listener
        pluginManager.registerListener(this, listener = new LoginListener(this));
        // Register commands
        pluginManager.registerCommand(this, new PluginCommand(this, "safenet"));
        pluginManager.registerCommand(this, new PluginCommand(this, "sn"));

        // If enabled
        if (config.getBoolean("metrics")) {
            // Initializing metrics
            getLogger().info("Initializing metrics.");
            // Initialize Metrics
            new Metrics(this, 6479);
        }
    }

    /**
     * Returns whether BungeeCord is of an unsupported version.
     *
     * @return whether BungeeCord is of an unsupported version
     */
    private boolean isUnsupportedBungeeCord() {
        try {
            Class.forName("net.md_5.bungee.protocol.Property");
            return false;
        } catch (ClassNotFoundException ex) {
            return true;
        }
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
     * Returns the passphrase vault.
     *
     * @return the passphrase vault
     */
    public PassphraseVault getPassphraseVault() {
        return passphraseVault;
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