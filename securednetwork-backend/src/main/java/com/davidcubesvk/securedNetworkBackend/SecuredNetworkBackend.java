package com.davidcubesvk.securedNetworkBackend;

import com.comphenix.protocol.ProtocolLibrary;
import com.davidcubesvk.securedNetworkBackend.command.PluginCommand;
import com.davidcubesvk.securedNetworkBackend.config.ConfigBackend;
import com.davidcubesvk.securedNetworkBackend.packet.PacketHandler;
import com.davidcubesvk.securedNetworkCore.authenticator.Authenticator;
import com.davidcubesvk.securedNetworkCore.config.Config;
import com.davidcubesvk.securedNetworkCore.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main class for the backend-side of the plugin.
 */
public class SecuredNetworkBackend extends JavaPlugin {

    //Plugin
    private Plugin plugin;

    //Config
    private Config config;
    //Logger
    private Log log;
    //Authenticator
    private Authenticator authenticator;
    //Packet handler
    private PacketHandler packetHandler;

    @Override
    public void onEnable() {
        //Set the plugin instance
        plugin = this;

        //Thank you message
        System.out.println("[SecuredNetwork] Thank you for downloading SecuredNetwork!");

        //Load the config file
        config = new ConfigBackend(this, "backend_config.yml", "config.yml");
        //Initialize log class
        log = new Log(getLogger(), new File(getDataFolder(), "logs"), config);

        //Enabling
        log.log(Level.INFO, Log.Source.GENERAL, "Enabling SecuredNetwork... (BACKEND)");

        //Initializing the authenticator
        log.log(Level.INFO, Log.Source.GENERAL, "Initializing the authenticator.");
        //Initialize
        authenticator = new Authenticator(config, log);

        //Registering listeners and commands
        log.log(Level.INFO, Log.Source.GENERAL, "Registering listeners and commands.");
        //Register commands
        Bukkit.getPluginCommand("securednetwork").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));

        //Registering the packet listener
        log.log(Level.INFO, Log.Source.GENERAL, "Registering the packet listener.");
        //Register
        packetHandler = new PacketHandler(ProtocolLibrary.getProtocolManager(), this);

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
