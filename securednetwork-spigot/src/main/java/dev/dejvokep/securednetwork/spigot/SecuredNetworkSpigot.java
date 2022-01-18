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
package dev.dejvokep.securednetwork.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import dev.dejvokep.boostedyaml.YamlFile;
import dev.dejvokep.securednetwork.core.authenticator.Authenticator;
import dev.dejvokep.securednetwork.core.config.Config;
import dev.dejvokep.securednetwork.spigot.command.PluginCommand;
import dev.dejvokep.securednetwork.spigot.packet.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Main class for the backend-side of the plugin.
 */
public class SecuredNetworkSpigot extends JavaPlugin {

    // Plugin
    private Plugin plugin;

    // Config
    private YamlFile config;

    // Authenticator
    private Authenticator authenticator;
    // Packet handler
    private PacketHandler packetHandler;

    @Override
    public void onEnable() {
        // Set the plugin instance
        plugin = this;

        // Thank you message
        System.out.println("[SecuredNetwork] Thank you for downloading SecuredNetwork!");

        try {
            // Load the config file
            config = Config.create(new File(getDataFolder(), "config.yml"), getResource("spigot_config.yml"));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize config file! Shutting down...", ex);
            Bukkit.shutdown();
            return;
        }

        // Initialize
        authenticator = new Authenticator(config, getLogger());
        // Register commands
        Bukkit.getPluginCommand("securednetwork").setExecutor(new PluginCommand(this));
        Bukkit.getPluginCommand("sn").setExecutor(new PluginCommand(this));
        // Register
        packetHandler = new PacketHandler(ProtocolLibrary.getProtocolManager(), this);
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
