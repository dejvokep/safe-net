package com.davidcubesvk.securedNetworkProxy.command;

import com.davidcubesvk.securedNetworkCore.config.Config;
import com.davidcubesvk.securedNetworkCore.log.Log;
import com.davidcubesvk.securedNetworkProxy.SecuredNetworkProxy;
import com.davidcubesvk.securedNetworkProxy.util.message.Messenger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.command.ConsoleCommandSender;

import java.util.logging.Level;

/**
 * Command executor for the main plugin command, which can reload the plugin or generate a new passphrase.
 */
public class PluginCommand extends net.md_5.bungee.api.plugin.Command {

    //The configuration file
    private final Config config;
    //The messenger
    private final Messenger messenger;
    //The plugin instance
    private final SecuredNetworkProxy plugin;

    /**
     * Constructor used to register the command.
     *
     * @param command the command name to register
     */
    public PluginCommand(SecuredNetworkProxy plugin, String command) {
        //Call the superclass constructor
        super(command);
        //Set
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.messenger = plugin.getMessenger();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        //Check the sender
        if (!(sender instanceof ConsoleCommandSender)) {
            //Console only
            messenger.sendMessage(sender, config.getString("command.console-only"));
            return;
        }

        //Command to execute
        String command;
        //Passphrase length to generate
        int passphraseLength = 0;

        //Check the arguments
        if ((args.length != 1 && args.length != 2) ||
                //If the command is not available
                (!(command = args[0].toLowerCase()).equals("reload") && !command.equals("generate")) ||
                //If the command is generate and the argument length is not 2, or the 2nd argument is invalid
                (command.equals("generate") && (args.length != 2 || (passphraseLength = toPassphraseLength(args[1])) == -1)) ||
                //If the command is not generate and the argument length is not 1
                (!command.equals("generate") && args.length != 1)) {
            //Invalid format
            messenger.sendMessage(sender, config.getString("command.invalid-format"));
            return;
        }

        //Execute the command
        if (command.equals("reload")) {
            //Reloading
            plugin.getLog().log(Level.INFO, Log.Source.GENERAL, "Reloading...");

            //Config
            config.load();
            //Authenticator
            plugin.getAuthenticator().reload();
            //IP whitelist
            plugin.getIpWhitelist().reload();
            //Updater
            plugin.getUpdater().reload();
            //Login listener
            plugin.getListener().reload();

            //Reloaded
            plugin.getLog().log(Level.INFO, Log.Source.GENERAL, "Reloaded.");
            messenger.sendMessage(sender, config.getString("command.reload"));
        } else {
            //Generate
            plugin.getAuthenticator().generatePassphrase(passphraseLength);
            //Generated
            messenger.sendMessage(sender, config.getString("command.generate"));
        }
    }

    /**
     * Parses the given value to an integer and returns it. The given string is considered suitable if:
     * <ul>
     *     <li>it is an integer,</li>
     *     <li>it's integer representation is not less than <code>1</code>.</li>
     * </ul>
     * If the string value does not meet all conditions listed above, the returned value is <code>-1</code> representing
     * an invalid string value.
     *
     * @param value the string value of the integer
     * @return the integer parsed from the given value
     */
    private int toPassphraseLength(String value) {
        //Parsed integer
        int parsed;

        //Try to parse
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }

        //If less than 1
        if (parsed < 1)
            return -1;

        return parsed;
    }
}
