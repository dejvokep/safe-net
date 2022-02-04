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

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.securednetwork.spigot.SecuredNetworkSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
        YamlDocument config = plugin.getConfiguration();
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
            // Config
            try {
                config.reload();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while loading the config! If you believe this is not caused by improper configuration, please report it.", ex);
                return true;
            }

            // Authenticator
            plugin.getAuthenticator().reload();
            // Packet handler
            plugin.getPacketHandler().reload();

            // Reloaded
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.reload")));
            return true;
        }

        // Invalid format
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("command.invalid-format")));
        return true;
    }
}
