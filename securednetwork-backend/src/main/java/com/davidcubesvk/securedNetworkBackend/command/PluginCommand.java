package com.davidcubesvk.securedNetworkBackend.command;

import com.davidcubesvk.securedNetworkBackend.SecuredNetworkBackend;
import com.davidcubesvk.securedNetworkCore.authenticator.Authenticator;
import com.davidcubesvk.securedNetworkCore.config.Config;
import com.davidcubesvk.securedNetworkCore.log.Log;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.logging.Level;

/**
 * Command executor for the main plugin command, which reloads the plugin.
 */
public class PluginCommand implements CommandExecutor {

    //The configuration file
    private Config config;
    //The log
    private Log log;
    //Authenticator
    private Authenticator authenticator;

    /**
     * Initializes the internals.
     *
     * @param plugin the plugin instance
     */
    public PluginCommand(SecuredNetworkBackend plugin) {
        //Set
        this.config = plugin.getConfiguration();
        this.log = plugin.getLog();
        this.authenticator = plugin.getAuthenticator();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        //Check the sender
        if (!(sender instanceof ConsoleCommandSender)) {
            //Console only
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.console-only")));
            return true;
        }

        //Check the arguments
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            //Invalid format
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.invalid-format")));
            return true;
        }

        //Reloading
        log.log(Level.INFO, Log.Source.GENERAL, "Reloading...");

        //Config
        config.load();
        //Authenticator
        authenticator.reload();

        //Reloaded
        log.log(Level.INFO, Log.Source.GENERAL, "Reloaded.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("command.reload")));
        return true;
    }
}
