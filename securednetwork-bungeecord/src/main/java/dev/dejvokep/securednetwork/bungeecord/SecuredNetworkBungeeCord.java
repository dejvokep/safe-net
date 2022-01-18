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
package dev.dejvokep.securednetwork.bungeecord;

import dev.dejvokep.boostedyaml.YamlFile;
import dev.dejvokep.securednetwork.bungeecord.command.PluginCommand;
import dev.dejvokep.securednetwork.bungeecord.ipwhitelist.IPWhitelist;
import dev.dejvokep.securednetwork.bungeecord.listener.LoginListener;
import dev.dejvokep.securednetwork.bungeecord.updater.Updater;
import dev.dejvokep.securednetwork.bungeecord.message.Messenger;
import dev.dejvokep.securednetwork.core.authenticator.Authenticator;
import dev.dejvokep.securednetwork.core.config.Config;
import dev.dejvokep.securednetwork.core.log.LogSource;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class for the proxy-side of the plugin.
 */
public class SecuredNetworkBungeeCord extends Plugin {

    // Plugin
    private Plugin plugin;

    // Message sender
    private final Messenger messenger = new Messenger();
    // Updater
    private Updater updater;
    // Config
    private YamlFile config;
    // Logger
    private Logger logger;
    private LoggerContext loggerContext;
    // Authenticator
    private Authenticator authenticator;
    // IP whitelist
    private IPWhitelist ipWhitelist;
    // Login listener
    private LoginListener listener;

    @Override
    public void onEnable() {
        try {
            // Context
            loggerContext = LoggerContext.getContext(getClass().getClassLoader(), false, Objects.requireNonNull(getClass().getClassLoader().getResource("log4j2_securednetwork.xml")).toURI());
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
            ProxyServer.getInstance().stop();
            return;
        }

        // Set the plugin instance
        plugin = this;
        // Thank you message
        getLogger().info("Thank you for downloading SecuredNetwork!");

        try {
            // Load the config file
            config = Config.create(new File(getDataFolder(), "config.yml"), getResourceAsStream("bungee_config.yml"));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize config file! Shutting down...", ex);
            getDedicatedLogger().error("Failed to initialize config file! Shutting down...", ex);
            ProxyServer.getInstance().stop();
            return;
        }

        // Enabling
        logger.info(LogSource.GENERAL.getPrefix() + "Enabling SecuredNetwork... (BungeeCord)");

        // Initializing the authenticator
        logger.info(LogSource.GENERAL.getPrefix() + "Initializing the authenticator.");
        // Initialize
        authenticator = new Authenticator(config, getLogger(), logger);

        // Initializing the IP whitelist
        logger.info(LogSource.GENERAL.getPrefix() + "Initializing the IP whitelist...");
        // Initialize
        ipWhitelist = new IPWhitelist(this);

        // Registering listeners and commands
        logger.info(LogSource.GENERAL.getPrefix() + "Registering listeners and commands.");
        // Plugin manager
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        // Register listener
        pluginManager.registerListener(this, listener = new LoginListener(this));
        // Register commands
        pluginManager.registerCommand(this, new PluginCommand(this, "securednetwork"));
        pluginManager.registerCommand(this, new PluginCommand(this, "sn"));

        // Starting updater
        logger.info(LogSource.GENERAL.getPrefix() + "Starting updater.");
        // Initialize
        updater = new Updater(this);

        // If enabled
        if (config.getBoolean("metrics")) {
            // Initializing metrics
            logger.info(LogSource.GENERAL.getPrefix() + "Initializing metrics.");
            // Initialize Metrics
            new Metrics(this, 6479);
        }

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
    public YamlFile getConfiguration() {
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
     * Returns the messenger.
     *
     * @return the messenger
     */
    public Messenger getMessenger() {
        return messenger;
    }

    /**
     * Returns the IP whitelist.
     *
     * @return the IP whitelist
     */
    public IPWhitelist getIpWhitelist() {
        return ipWhitelist;
    }

    /**
     * Returns the updater.
     *
     * @return the updater
     */
    public Updater getUpdater() {
        return updater;
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