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
import dev.dejvokep.safenet.spigot.listener.PacketListener;
import dev.dejvokep.safenet.spigot.listener.SessionListener;
import dev.dejvokep.safenet.spigot.disconnect.DisconnectHandler;
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
    // Packet listener
    private PacketListener packetListener;
    // Protocol disconnect
    private DisconnectHandler disconnectHandler;

    // Authenticator
    private Authenticator authenticator;

    @Override
    public void onEnable() {
        // Thank you message
        getLogger().info("Thank you for downloading SafeNET!");

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
        authenticator = new Authenticator(passphraseStore, getLogger());
        // Register commands
        Bukkit.getPluginCommand("safenet").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));
        // Register
        packetListener = new PacketListener(this);
        new SessionListener(this);
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
     * Returns the packet listener.
     *
     * @return the packet listener
     */
    public PacketListener getPacketListener() {
        return packetListener;
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

}
