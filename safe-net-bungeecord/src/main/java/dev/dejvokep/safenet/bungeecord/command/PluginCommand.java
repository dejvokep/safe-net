/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.safenet.bungeecord.command;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.safenet.bungeecord.SafeNetBungeeCord;
import dev.dejvokep.safenet.bungeecord.message.Messenger;
import dev.dejvokep.safenet.core.PassphraseVault;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Command executor for the main plugin command.
 */
public class PluginCommand extends Command {

    // Config
    private final YamlDocument config;
    // Messenger
    private final Messenger messenger;
    // Plugin
    private final SafeNetBungeeCord plugin;

    /**
     * Constructor used to register the command.
     *
     * @param command the command name to register
     */
    public PluginCommand(@NotNull SafeNetBungeeCord plugin, @NotNull String command) {
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
                        plugin.getLogger().log(Level.SEVERE, "An error occurred whilst loading the config!", ex);
                        return;
                    }
                    // Authenticator
                    plugin.getPassphraseVault().reload();
                    // Address whitelist
                    plugin.getAddressWhitelist().reload();
                    // Login listener
                    plugin.getListener().reload();

                    // Reloaded
                    messenger.sendMessage(sender, config.getString("command.reload"));
                    return;
                case "diagnostics":
                    messenger.sendMessages(sender, String.format("Plugin: %s v%s", plugin.getDescription().getName(), plugin.getDescription().getVersion()),
                            String.format("Passphrase: %s (%d chars)", plugin.getPassphraseVault().getPassphraseStatus(), plugin.getPassphraseVault().getPassphrase().length()),
                            String.format("Address whitelist: %s (%d entries)", (plugin.getAddressWhitelist().isEnabled() ? "enabled" : "disabled"), plugin.getAddressWhitelist().getAddresses().size()),
                            String.format("Server: %s %s", ProxyServer.getInstance().getName(), ProxyServer.getInstance().getVersion()),
                            String.format("Java: %s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor")),
                            String.format("Java VM: %s (%s), %s", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.info")),
                            String.format("OS: %s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")));
                    return;
            }
        }

        // If to generate
        if (args.length <= 2 && args[0].equalsIgnoreCase("generate")) {
            try {
                // Generate
                plugin.getPassphraseVault().generatePassphrase(args.length == 1 ? PassphraseVault.RECOMMENDED_PASSPHRASE_LENGTH : toPassphraseLength(args[1]));
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred whilst saving the config!", ex);
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
