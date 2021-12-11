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
package dev.dejvokep.securednetwork.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import dev.dejvokep.securednetwork.core.authenticator.Authenticator;
import dev.dejvokep.securednetwork.core.config.Config;
import dev.dejvokep.securednetwork.core.log.Log;
import dev.dejvokep.securednetwork.spigot.command.PluginCommand;
import dev.dejvokep.securednetwork.spigot.config.ConfigBackend;
import dev.dejvokep.securednetwork.spigot.packet.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main class for the backend-side of the plugin.
 */
public class SecuredNetworkSpigot extends JavaPlugin {

    // Plugin
    private Plugin plugin;

    // Config
    private Config config;
    // Logger
    private Log log;
    // Authenticator
    private Authenticator authenticator;
    // Packet handler
    private PacketHandler packetHandler;

    @Override
    public void onEnable() {
        // Set the plugin instance
        plugin = this;

        // Thank you message
        System.out.println("[SecuredNetwork] Thank you for downloading SecuredNetwork!");

        // Load the config file
        config = new ConfigBackend(this, "backend_config.yml", "config.yml");
        // Initialize log class
        log = new Log(getLogger(), new File(getDataFolder(), "logs"), config);

        // Enabling
        log.log(Level.INFO, Log.Source.GENERAL, "Enabling SecuredNetwork... (BACKEND)");

        // Initializing the authenticator
        log.log(Level.INFO, Log.Source.GENERAL, "Initializing the authenticator.");
        // Initialize
        authenticator = new Authenticator(config, log);

        // Registering listeners and commands
        log.log(Level.INFO, Log.Source.GENERAL, "Registering listeners and commands.");
        // Register commands
        Bukkit.getPluginCommand("securednetwork").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));

        // Registering the packet listener
        log.log(Level.INFO, Log.Source.GENERAL, "Registering the packet listener.");
        // Register
        packetHandler = new PacketHandler(ProtocolLibrary.getProtocolManager(), this);

        // Finished enabling
        log.log(Level.INFO, Log.Source.GENERAL, "Finished enabling SecuredNetwork.");
    }

    @Override
    public void onDisable() {
        // Finished disabling
        log.log(Level.INFO, Log.Source.GENERAL, "Finished disabling SecuredNetwork.");
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
    public Config getConfiguration() {
        return config;
    }

    /**
     * Returns the logging utility which is used to log plugin messages.
     *
     * @return the logging utility
     */
    public Log getLog() {
        return log;
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
     * Returns the packet handler.
     *
     * @return the packet handler
     */
    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

}
