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
package dev.dejvokep.safenet.spigot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.safenet.core.PassphraseStore;
import dev.dejvokep.safenet.spigot.authentication.Authenticator;
import dev.dejvokep.safenet.spigot.command.PluginCommand;
import dev.dejvokep.safenet.spigot.disconnect.DisconnectHandler;
import dev.dejvokep.safenet.spigot.listener.HandshakeListener;
import dev.dejvokep.safenet.spigot.listener.SessionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class for the backend-side of the plugin.
 */
public class SafeNetSpigot extends JavaPlugin {

    // Config
    private YamlDocument config;

    // Passphrase store
    private PassphraseStore passphraseStore;
    // Handshake listener
    private HandshakeListener handshakeListener;
    // Protocol disconnect
    private DisconnectHandler disconnectHandler;

    // Authenticator
    private Authenticator authenticator;
    // Paper
    private boolean paperServer;

    @Override
    public void onEnable() {
        try {
            // Create the config file
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), Objects.requireNonNull(getResource("spigot-config.yml")), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize the config file! Shutting down...", ex);
            Bukkit.shutdown();
            return;
        }

        // Initialize
        passphraseStore = new PassphraseStore(config, getLogger());
        disconnectHandler = new DisconnectHandler(this);
        authenticator = new Authenticator(this);
        // Register commands
        Bukkit.getPluginCommand("safenet").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));
        // Register
        handshakeListener = new HandshakeListener(this);

        // Paper server
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paperServer = true;
        } catch (ClassNotFoundException ignored) {
            paperServer = false;
        }

        // If not a paper server
        if (!paperServer)
            // All packets are held until all plugins are initialized, so the listener is guaranteed to always be registered
            new SessionListener(this);
        else
            getLogger().info("Paper (or forked) server detected; sessions will not be validated.");

        // Postpone messages
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Thank you message
            getLogger().info("Thank you for downloading SafeNET!");
            // Print
            passphraseStore.printStatus();
        }, 1);
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
     * Returns the passphrase store.
     *
     * @return the passphrase store
     */
    public PassphraseStore getPassphraseStore() {
        return passphraseStore;
    }

    /**
     * Returns the handshake listener.
     *
     * @return the handshake listener
     */
    public HandshakeListener getHandshakeListener() {
        return handshakeListener;
    }

    /**
     * Returns the disconnect handler.
     *
     * @return the disconnect handler
     */
    public DisconnectHandler getDisconnectHandler() {
        return disconnectHandler;
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
     * Returns whether this is a Paper (or forked) server.
     *
     * @return if this is a Paper based server
     */
    public boolean isPaperServer() {
        return paperServer;
    }
}
