package com.davidcubesvk.securedNetworkProxy;

import com.davidcubesvk.securedNetworkCore.authenticator.Authenticator;
import com.davidcubesvk.securedNetworkCore.config.Config;
import com.davidcubesvk.securedNetworkCore.log.Log;
import com.davidcubesvk.securedNetworkProxy.command.PluginCommand;
import com.davidcubesvk.securedNetworkProxy.ipWhitelist.IPWhitelist;
import com.davidcubesvk.securedNetworkProxy.listener.LoginListener;
import com.davidcubesvk.securedNetworkProxy.updater.Updater;
import com.davidcubesvk.securedNetworkProxy.util.config.ConfigProxy;
import com.davidcubesvk.securedNetworkProxy.util.message.Messenger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.util.logging.Level;

/**
 * Main class for the proxy-side of the plugin.
 */
public class SecuredNetworkProxy extends Plugin {

    //Plugin
    private Plugin plugin;

    //Message sender
    private final Messenger messenger = new Messenger();
    //Updater
    private Updater updater;
    //Config
    private Config config;
    //Logger
    private Log log;
    //Authenticator
    private Authenticator authenticator;
    //IP whitelist
    private IPWhitelist ipWhitelist;
    //Login listener
    private LoginListener listener;

    @Override
    public void onEnable() {
        //Set the plugin instance
        plugin = this;
        //Thank you message
        getLogger().info("Thank you for downloading SecuredNetwork!");

        //Load the config file
        config = new ConfigProxy(this, "proxy_config.yml", "config.yml");
        //Initialize log class
        log = new Log(getLogger(), new File(getDataFolder(), "logs"), config);

        //Enabling
        log.log(Level.INFO, Log.Source.GENERAL, "Enabling SecuredNetwork... (PROXY)");

        //Initializing the authenticator
        log.log(Level.INFO, Log.Source.GENERAL, "Initializing the authenticator.");
        //Initialize
        authenticator = new Authenticator(config, log);

        //Initializing the IP whitelist
        log.log(Level.INFO, Log.Source.GENERAL, "Initializing the IP whitelist...");
        //Initialize
        ipWhitelist = new IPWhitelist(this);

        //Registering listeners and commands
        log.log(Level.INFO, Log.Source.GENERAL, "Registering listeners and commands.");
        //Plugin manager
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        //Register listener
        pluginManager.registerListener(this, listener = new LoginListener(this));
        //Register commands
        pluginManager.registerCommand(this, new PluginCommand(this, "securednetwork"));
        pluginManager.registerCommand(this, new PluginCommand(this, "sn"));

        //Starting updater
        log.log(Level.INFO, Log.Source.GENERAL, "Starting updater.");
        //Initialize
        updater = new Updater(this);

        //If enabled
        if (config.getBoolean("metrics")) {
            //Initializing metrics
            log.log(Level.INFO, Log.Source.GENERAL, "Initializing metrics.");
            //Initialize Metrics
            new Metrics(this, 6479);
        }

        //Finished enabling
        log.log(Level.INFO, Log.Source.GENERAL, "Finished enabling SecuredNetwork.");
    }

    @Override
    public void onDisable() {
        //Finished disabling
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
     * @return the configuration file.
     */
    public Config getConfiguration() {
        return config;
    }

    /**
     * Returns the logging utility which is used to log plugin messages.
     * @return the logging utility
     */
    public Log getLog() {
        return log;
    }

    /**
     * Returns the authenticator.
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
     * @return the login event listener
     */
    public LoginListener getListener() {
        return listener;
    }

}