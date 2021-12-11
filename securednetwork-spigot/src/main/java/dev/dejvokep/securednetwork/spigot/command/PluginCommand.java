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
package dev.dejvokep.securednetwork.spigot.command;

import dev.dejvokep.securednetwork.core.config.Config;
import dev.dejvokep.securednetwork.core.log.Log;
import dev.dejvokep.securednetwork.spigot.SecuredNetworkSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Command executor for the main plugin command, which reloads the plugin.
 */
public class PluginCommand implements CommandExecutor {

    // The plugin instance
    private final SecuredNetworkSpigot plugin;

    /**
     * Initializes the internals.
     *
     * @param plugin the plugin instance
     */
    public PluginCommand(@NotNull SecuredNetworkSpigot plugin) {
        // Set
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        // The config
        Config config = plugin.getConfiguration();
        // Check the sender
        if (!(sender instanceof ConsoleCommandSender)) {
            // Console only
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.console-only")));
            return true;
        }

        // If less than 1 argument
        if (args.length < 1) {
            // Invalid format
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.invalid-format")));
            return true;
        }

        // If to reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // Reloading
            plugin.getLog().log(Level.INFO, Log.Source.GENERAL, "Reloading...");

            // Config
            config.load();
            // Authenticator
            plugin.getAuthenticator().reload();
            // Packet handler
            plugin.getPacketHandler().reload();

            // Reloaded
            plugin.getLog().log(Level.INFO, Log.Source.GENERAL, "Reloaded.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.reload")));
            return true;
        }

        // If to manage the connection logger
        if (args.length >= 2 && args[0].equalsIgnoreCase("connection-logger")) {
            // If to detach
            if (args.length == 2 && args[1].equalsIgnoreCase("detach")) {
                // Detach
                plugin.getPacketHandler().getConnectionLogger().detach();
                // Log
                plugin.getLog().log(Level.INFO, Log.Source.CONNECTOR, "Connection logger detached.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        config.getString("command.connection-logger.detached")));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("attach")) {
                // Replace all dashes
                args[2] = args[2].replace("-", "");
                // Attach
                plugin.getPacketHandler().getConnectionLogger().attach(args[2]);
                // Log
                plugin.getLog().log(Level.INFO, Log.Source.CONNECTOR, "Connection logger attached to UUID \"" + args[2] + "\".");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        config.getString("command.connection-logger.attached").replace("{uuid}", args[2])));
                return true;
            }
        }

        // Invalid format
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("command.invalid-format")));
        return true;
    }
}
