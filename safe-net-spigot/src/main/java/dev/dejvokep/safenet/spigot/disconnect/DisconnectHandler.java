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
package dev.dejvokep.safenet.spigot.disconnect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class responsible for handling player disconnection.
 */
public class DisconnectHandler {

    /**
     * Default disconnect message used if the one provided in the config is invalid.
     */
    private static final String DEFAULT_DISCONNECT_MESSAGE = "Disconnected";

    // Disconnect message
    private String message;
    // Plugin
    private final SafeNetSpigot plugin;

    /**
     * Initializes the internal data.
     *
     * @param plugin the plugin
     */
    public DisconnectHandler(@NotNull SafeNetSpigot plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Disconnects the given player immediately via the protocol.
     * <p>
     * <b>To be used only during the login phase. This method is not exception-safe.</b>
     *
     * @param player the player to disconnect
     */
    @SuppressWarnings("deprecation")
    public void login(@NotNull Player player) {
        try {
            // Create the disconnect packet
            PacketContainer disconnectPacket = new PacketContainer(PacketType.Login.Server.DISCONNECT);
            // Write
            disconnectPacket.getModifier().writeDefaults();
            String serialized = ComponentSerializer.toString(TextComponent.fromLegacyText(message));
            disconnectPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(serialized));
            // Send
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, disconnectPacket);
        } catch (Exception ignored) {
        }

        // Disconnect the player
        TemporaryPlayerFactory.getInjectorFromPlayer(player).disconnect(message);
    }

    /**
     * Disconnects the given player using the server API. If the result of the kicking the player is important, the
     * caller must watch the call chain using server events.
     *
     * @param player the player to disconnect
     */
    public void play(@NotNull Player player) {
        player.kickPlayer(message);
    }

    /**
     * Reloads the internal configuration.
     */
    public void reload() {
        message = ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getString("disconnect-message", DEFAULT_DISCONNECT_MESSAGE));
    }

    /**
     * Returns the disconnect message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

}