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
package dev.dejvokep.safenet.spigot.command;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Command executor for the main plugin command.
 */
public class PluginCommand implements CommandExecutor {

    // Plugin
    private final SafeNetSpigot plugin;

    /**
     * Initializes the command executor.
     *
     * @param plugin the plugin
     */
    public PluginCommand(@NotNull SafeNetSpigot plugin) {
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

        // If not 1 argument
        if (args.length != 1) {
            // Invalid format
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("command.invalid-format")));
            return true;
        }

        // Switch
        switch (args[0].toLowerCase()) {
            case "reload":
                // Config
                try {
                    config.reload();
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred whilst loading the config!", ex);
                    return true;
                }

                // Reload all
                plugin.getHandshakeListener().reload();
                plugin.getPassphraseVault().reload();
                plugin.getDisconnectHandler().reload();
                plugin.getAuthenticator().reload();

                // Reloaded
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        config.getString("command.reload")));
                return true;
            case "diagnostics":
                sender.sendMessage(String.format("Plugin: %s v%s", plugin.getDescription().getName(), plugin.getDescription().getVersion()),
                        String.format("Passphrase: %s (%d chars)", plugin.getPassphraseVault().getPassphraseStatus(), plugin.getPassphraseVault().getPassphrase().length()),
                        String.format("Mode: %s", plugin.getHandshakeListener().getSignature()),
                        String.format("Server: %s %s %s", Bukkit.getName(), Bukkit.getVersion(), Bukkit.getBukkitVersion()),
                        String.format("Java: %s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor")),
                        String.format("Java VM: %s (%s), %s", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.info")),
                        String.format("OS: %s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")));
                return true;
        }

        // Invalid format
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("command.invalid-format")));
        return true;
    }
}
