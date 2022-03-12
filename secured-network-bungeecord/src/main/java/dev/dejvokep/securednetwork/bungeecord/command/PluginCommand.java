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
package dev.dejvokep.securednetwork.bungeecord.command;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.securednetwork.bungeecord.SecuredNetworkBungeeCord;
import dev.dejvokep.securednetwork.bungeecord.message.Messenger;
import dev.dejvokep.securednetwork.core.authenticator.Authenticator;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Command executor for the main plugin command, which can reload the plugin or generate a new passphrase.
 */
public class PluginCommand extends Command {

    // The config
    private final YamlDocument config;
    // The messenger
    private final Messenger messenger;
    // The plugin instance
    private final SecuredNetworkBungeeCord plugin;

    /**
     * Constructor used to register the command.
     *
     * @param command the command name to register
     */
    public PluginCommand(@NotNull SecuredNetworkBungeeCord plugin, @NotNull String command) {
        // Call the superclass constructor
        super(command);
        // Set
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.messenger = plugin.getMessenger();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check the sender
        if (!(sender instanceof ConsoleCommandSender)) {
            // Console only
            messenger.sendMessage(sender, config.getString("command.console-only"));
            return;
        }

        // If less than 1 argument
        if (args.length < 1) {
            // Invalid format
            messenger.sendMessage(sender, config.getString("command.invalid-format"));
            return;
        }

        // If no additional argument
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    // Config
                    try {
                        config.reload();
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while loading the config! If you believe this is not caused by improper configuration, please report it.", ex);
                        return;
                    }
                    // Authenticator
                    plugin.getAuthenticator().reload();
                    // Address whitelist
                    plugin.getAddressWhitelist().reload();
                    // Login listener
                    plugin.getListener().reload();

                    // Reloaded
                    messenger.sendMessage(sender, config.getString("command.reload"));
                    return;
                case "diagnostics":
                    messenger.sendMessage(sender, "Plugin: " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
                    messenger.sendMessage(sender, "Passphrase: " + plugin.getAuthenticator().getPassphraseStatus() + " (" + plugin.getAuthenticator().getPassphrase().length() + " chars)");
                    messenger.sendMessage(sender, "Address whitelist: " + (plugin.getAddressWhitelist().isEnabled() ? "enabled" : "disabled") + " (" + plugin.getAddressWhitelist().getAddresses().size() + " entries)");
                    messenger.sendMessage(sender, "Server: " + ProxyServer.getInstance().getName() + " " + ProxyServer.getInstance().getVersion());
                    messenger.sendMessage(sender, "Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
                    messenger.sendMessage(sender, "Java VM: " + System.getProperty("java.vm.name") + System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.version") + "), " + System.getProperty("java.vm.info"));
                    messenger.sendMessage(sender, "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
                    return;
            }
        }

        // If to generate
        if (args.length <= 2 && args[0].equalsIgnoreCase("generate")) {
            try {
                // Generate
                plugin.getAuthenticator().generatePassphrase(args.length == 1 ? Authenticator.RECOMMENDED_PASSPHRASE_LENGTH : toPassphraseLength(args[1]));
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while saving the config!", ex);
                return;
            }
            // Generated
            messenger.sendMessage(sender, config.getString("command.generate"));
            return;
        }

        // Invalid format
        messenger.sendMessage(sender, config.getString("command.invalid-format"));
    }

    /**
     * Parses the given value to an integer and returns it. The given string is considered suitable if:
     * <ul>
     *     <li>it is an integer,</li>
     *     <li>it is not less than <code>1</code>.</li>
     * </ul>
     * If the string value does not meet the conditions listed above, the returned value is <code>-1</code>, representing
     * an invalid string value.
     *
     * @param value the string value of the integer
     * @return the integer parsed from the given value
     */
    private int toPassphraseLength(@NotNull String value) {
        // Parsed integer
        int parsed;

        // Try to parse
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }

        // If less than 1
        if (parsed < 1)
            return -1;

        return parsed;
    }
}
