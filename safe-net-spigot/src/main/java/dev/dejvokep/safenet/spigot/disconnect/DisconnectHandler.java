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
package dev.dejvokep.safenet.spigot.disconnect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Class responsible for handling player disconnection.
 */
public class DisconnectHandler {

    /**
     * Default disconnect message used if the one provided in the config is invalid.
     */
    private static final String DEFAULT_DISCONNECT_MESSAGE = "Disconnected";

    /**
     * CraftBukkit package name.
     */
    private static final String PACKAGE_CRAFT_BUKKIT = Bukkit.getServer().getClass().getPackage().getName();

    /**
     * Vanilla server package name.
     */
    private static final String PACKAGE_GAME_SERVER = PACKAGE_CRAFT_BUKKIT.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    /**
     * Classes used to disconnect players using server internals.
     */
    public static Class<?> CRAFT_PLAYER_CLASS, ENTITY_PLAYER_CLASS, KICK_PACKET_CLASS, CHAT_COMPONENT_TEXT_CLASS, CHAT_BASE_COMPONENT_CLASS, PACKET_CLASS, PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS, SERVER_CLASS;
    /**
     * Constructors used to disconnect players using server internals.
     */
    public static Constructor<?> KICK_PACKET_CONSTRUCTOR, CHAT_COMPONENT_TEXT_CONSTRUCTOR;
    /**
     * Methods used to disconnect players using server internals.
     */
    public static Method CRAFT_PLAYER_HANDLE_METHOD, NETWORK_MANAGER_SEND_PACKET_METHOD, NETWORK_MANAGER_CLOSE_METHOD, NETWORK_MANAGER_STOP_READING, NETWORK_MANAGER_HANDLE_DISCONNECTION, PLAYER_CONNECTION_CALL_DISCONNECT, SERVER_POST_TO_MAIN_THREAD_METHOD;
    /**
     * Fields used to disconnect players using server internals.
     */
    public static Field ENTITY_PLAYER_CONNECTION_FIELD, PLAYER_CONNECTION_MANAGER_FIELD, PLAYER_CONNECTION_SERVER_FIELD;

    // Disconnect message
    private String message;
    private Object chatComponentMessage;
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
    public void login(@NotNull Player player) {
        try {
            // Create the disconnect packet
            PacketContainer disconnectPacket = new PacketContainer(PacketType.Login.Server.DISCONNECT);
            // Write defaults
            disconnectPacket.getModifier().writeDefaults();
            BaseComponent[] textComponent = TextComponent.fromLegacyText(message);
            String serialized = ComponentSerializer.toString(textComponent);
            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(serialized);
            // Set the message
            disconnectPacket.getChatComponents().write(0, wrappedChatComponent);
            // Send
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, disconnectPacket);
        } catch (Exception ignored) {
        }

        // Disconnect the player
        TemporaryPlayerFactory.getInjectorFromPlayer(player).disconnect(message);
    }

    /**
     * Disconnects the given player immediately via server internals, without calling any events.
     * <p>
     * <b>To be used only during the play phase. This method is not exception-safe.</b>
     *
     * @param player the player to disconnect
     * @throws ReflectiveOperationException a reflection exception
     * @throws NullPointerException         a null pointer exception, usually thrown due to uninitialized reflection
     *                                      components (see startup log)
     */
    public void play(Player player) throws ReflectiveOperationException, NullPointerException {
        // Internals
        Object playerConnection = ENTITY_PLAYER_CONNECTION_FIELD.get(CRAFT_PLAYER_HANDLE_METHOD.invoke(CRAFT_PLAYER_CLASS.cast(player)));
        Object networkManager = PLAYER_CONNECTION_MANAGER_FIELD.get(playerConnection);
        Object server = PLAYER_CONNECTION_SERVER_FIELD.get(playerConnection);

        // Disconnect
        NETWORK_MANAGER_SEND_PACKET_METHOD.invoke(networkManager, KICK_PACKET_CONSTRUCTOR.newInstance(chatComponentMessage),
                (GenericFutureListener<Future<?>>) future -> NETWORK_MANAGER_CLOSE_METHOD.invoke(networkManager, chatComponentMessage), new GenericFutureListener[0]);
        PLAYER_CONNECTION_CALL_DISCONNECT.invoke(playerConnection, chatComponentMessage);
        NETWORK_MANAGER_STOP_READING.invoke(networkManager);

        // Call the server
        SERVER_POST_TO_MAIN_THREAD_METHOD.invoke(server, (Runnable) () -> {
            try {
                NETWORK_MANAGER_HANDLE_DISCONNECTION.invoke(networkManager);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, String.format("Server could not handle disconnection of \"%s\" (%s)!", player.getName(), player.getUniqueId()), ex);
            }
        });
    }

    /**
     * Reloads the internal configuration.
     */
    public void reload() {
        // Message
        message = ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getString("disconnect-message", DEFAULT_DISCONNECT_MESSAGE));
        // Create chat component
        try {
            chatComponentMessage = CHAT_COMPONENT_TEXT_CONSTRUCTOR.newInstance(message);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize internal server classes to utilize the disconnect message!", ex);
        }
    }

    /**
     * Returns the disconnect message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    static {
        try {
            CRAFT_PLAYER_CLASS = Class.forName(PACKAGE_CRAFT_BUKKIT + ".entity.CraftPlayer");
            ENTITY_PLAYER_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".EntityPlayer");
            KICK_PACKET_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".PacketPlayOutKickDisconnect");
            CHAT_COMPONENT_TEXT_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".ChatComponentText");
            CHAT_BASE_COMPONENT_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".IChatBaseComponent");
            PACKET_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".Packet");
            PLAYER_CONNECTION_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".PlayerConnection");
            NETWORK_MANAGER_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".NetworkManager");
            SERVER_CLASS = Class.forName(PACKAGE_GAME_SERVER + ".MinecraftServer");
            KICK_PACKET_CONSTRUCTOR = KICK_PACKET_CLASS.getConstructor(CHAT_BASE_COMPONENT_CLASS);
            CHAT_COMPONENT_TEXT_CONSTRUCTOR = CHAT_COMPONENT_TEXT_CLASS.getConstructor(String.class);
            CRAFT_PLAYER_HANDLE_METHOD = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");
            NETWORK_MANAGER_SEND_PACKET_METHOD = NETWORK_MANAGER_CLASS.getDeclaredMethod("a", PACKET_CLASS, GenericFutureListener.class, GenericFutureListener[].class);
            NETWORK_MANAGER_CLOSE_METHOD = NETWORK_MANAGER_CLASS.getMethod("close", CHAT_BASE_COMPONENT_CLASS);
            NETWORK_MANAGER_STOP_READING = NETWORK_MANAGER_CLASS.getDeclaredMethod("k");
            NETWORK_MANAGER_HANDLE_DISCONNECTION = NETWORK_MANAGER_CLASS.getDeclaredMethod("l");
            PLAYER_CONNECTION_CALL_DISCONNECT = PLAYER_CONNECTION_CLASS.getMethod("a", CHAT_BASE_COMPONENT_CLASS);
            SERVER_POST_TO_MAIN_THREAD_METHOD = SERVER_CLASS.getMethod("postToMainThread", Runnable.class);
            ENTITY_PLAYER_CONNECTION_FIELD = ENTITY_PLAYER_CLASS.getField("playerConnection");
            PLAYER_CONNECTION_MANAGER_FIELD = PLAYER_CONNECTION_CLASS.getField("networkManager");
            PLAYER_CONNECTION_SERVER_FIELD = PLAYER_CONNECTION_CLASS.getDeclaredField("minecraftServer");

            PLAYER_CONNECTION_MANAGER_FIELD.setAccessible(true);
            PLAYER_CONNECTION_SERVER_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

}