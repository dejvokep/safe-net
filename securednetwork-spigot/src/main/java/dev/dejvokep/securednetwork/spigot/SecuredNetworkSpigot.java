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
import dev.dejvokep.securednetwork.core.log.LogSource;
import dev.dejvokep.securednetwork.spigot.command.PluginCommand;
import dev.dejvokep.securednetwork.spigot.config.ConfigBackend;
import dev.dejvokep.securednetwork.spigot.packet.PacketHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.Objects;
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
    private Logger logger;
    private LoggerContext loggerContext;

    // Authenticator
    private Authenticator authenticator;
    // Packet handler
    private PacketHandler packetHandler;

    @Override
    public void onEnable() {
        try {
            // Context
            loggerContext = LoggerContext.getContext(getClassLoader(), false, Objects.requireNonNull(getClassLoader().getResource("log4j2-securednetwork.xml")).toURI());
            // Initialize the logger
            logger = loggerContext.getLogger(getClass().getName());
            // Display notice
            logger.info(LogSource.GENERAL.getPrefix() + "-------------------------------- IMPORTANT! --------------------------------");
            logger.info(LogSource.GENERAL.getPrefix() + "Log files may contain sensitive information such as server IP, player names,");
            logger.info(LogSource.GENERAL.getPrefix() + "player UUIDs, etc. If sharing, please make sure you delete ALL of these information.");
            logger.info(LogSource.GENERAL.getPrefix() + "Logs should be shared only with people you trust.");
            logger.info(LogSource.GENERAL.getPrefix() + "----------------------------------------------------------------------------");
        } catch (URISyntaxException | NullPointerException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize Log4j! Shutting down...", ex);
            Bukkit.shutdown();
        }
        // Set the plugin instance
        plugin = this;

        // Thank you message
        System.out.println("[SecuredNetwork] Thank you for downloading SecuredNetwork!");

        // Load the config file
        config = new ConfigBackend(this, "backend_config.yml", "config.yml");

        // Enabling
        logger.info(LogSource.GENERAL.getPrefix() + "Enabling SecuredNetwork... (Spigot)");

        // Initializing the authenticator
        logger.info(LogSource.GENERAL.getPrefix() + "Initializing the authenticator.");
        // Initialize
        authenticator = new Authenticator(config, getLogger(), logger);

        // Registering listeners and commands
        logger.info(LogSource.GENERAL.getPrefix() + "Registering listeners and commands.");
        // Register commands
        Bukkit.getPluginCommand("securednetwork").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));

        // Registering the packet listener
        logger.info(LogSource.GENERAL.getPrefix() + "Registering the packet listener.");
        // Register
        packetHandler = new PacketHandler(ProtocolLibrary.getProtocolManager(), this);

        // Finished enabling
        logger.info(LogSource.GENERAL.getPrefix() + "Finished enabling SecuredNetwork.");
    }

    @Override
    public void onDisable() {
        // Finished disabling
        logger.info(LogSource.GENERAL.getPrefix() + "Finished disabling SecuredNetwork.");
        loggerContext.terminate();
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
     * Returns the plugin's dedicated logging system.
     *
     * @return the dedicated logging system
     */
    public Logger getDedicatedLogger() {
        return logger;
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
