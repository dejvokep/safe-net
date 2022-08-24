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
import dev.dejvokep.safenet.spigot.listener.*;
import dev.dejvokep.safenet.spigot.listener.handshake.AbstractHandshakeListener;
import dev.dejvokep.safenet.spigot.listener.handshake.NativeHandshakeListener;
import dev.dejvokep.safenet.spigot.listener.handshake.PaperHandshakeListener;
import dev.dejvokep.safenet.spigot.listener.session.SessionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class for the backend-side of the plugin.
 */
public class SafeNetSpigot extends JavaPlugin {

    /**
     * Supported version of the ProtocolLib plugin.
     */
    private static final String PROTOCOL_LIB_VERSION = "5.0.0 or newer";

    /**
     * Paper's handshake event class.
     */
    private static final String PAPER_HANDSHAKE_EVENT = "com.destroystokyo.paper.event.player.PlayerHandshakeEvent";

    // Config
    private YamlDocument config;

    // Internals
    private PassphraseStore passphraseStore;
    private AbstractHandshakeListener handshakeListener;
    private DisconnectHandler disconnectHandler;
    private ListenerPusher listenerPusher;

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
        listenerPusher = new ListenerPusher(this);
        // Register commands
        Bukkit.getPluginCommand("safenet").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));

        // Paper server
        paperServer = classExists(PAPER_HANDSHAKE_EVENT);

        // If not a paper server
        if (!paperServer) {
            // ProtocolLib plugin
            Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
            // If ProtocolLib is not installed or is not a supported version
            if (!Bukkit.getPluginManager().isPluginEnabled(protocolLib) || isUnsupportedProtocolLib()) {
                getLogger().severe(String.format("This version of SafeNET requires ProtocolLib %s to run! Shutting down...", PROTOCOL_LIB_VERSION));
                Bukkit.shutdown();
                return;
            }

            // Register handshake listener
            try {
                handshakeListener = new NativeHandshakeListener(this);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "An unknown error has occurred whilst registering the packet listener! Shutting down...", ex);
                Bukkit.shutdown();
                return;
            }

            // Register session listeners
            new SessionListener(this);
            getLogger().info("Spigot native server detected; handshakes will be handled via the packet listener and sessions will be validated using the API.");
        } else {
            // All packets are held until all plugins are initialized, so the listener is guaranteed to always be registered
            getLogger().info("Paper (or forked) server detected; handshakes will be handled via the API and sessions will not be validated.");
            handshakeListener = new PaperHandshakeListener(this);
        }

        // Postpone messages
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Thank you message
            getLogger().info("Thank you for downloading SafeNET!");
            // Print
            passphraseStore.printStatus();
        }, 1);
    }

    /**
     * Returns whether a class by the given name exists.
     *
     * @param name the name to check for
     * @return whether a class by the given name exists
     * @see Class#forName(String)
     */
    public boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * Returns whether ProtocolLib is of an unsupported version.
     *
     * @return whether ProtocolLib is of an unsupported version
     */
    private boolean isUnsupportedProtocolLib() {
        return !classExists("com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory");
    }

    /**
     * Returns the authenticator.
     *
     * @return the authenticator
     */
    @NotNull
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Returns the passphrase store.
     *
     * @return the passphrase store
     */
    @NotNull
    public PassphraseStore getPassphraseStore() {
        return passphraseStore;
    }

    /**
     * Returns the handshake listener.
     *
     * @return the handshake listener
     */
    @NotNull
    public AbstractHandshakeListener getHandshakeListener() {
        return handshakeListener;
    }

    /**
     * Returns the disconnect handler.
     *
     * @return the disconnect handler
     */
    @NotNull
    public DisconnectHandler getDisconnectHandler() {
        return disconnectHandler;
    }

    /**
     * Returns the event pusher.
     *
     * @return the event pusher
     */
    @NotNull
    public ListenerPusher getEventPusher() {
        return listenerPusher;
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
