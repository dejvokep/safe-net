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
package dev.dejvokep.safenet.spigot.listener.handshake;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import dev.dejvokep.safenet.spigot.authentication.Authenticator;
import dev.dejvokep.safenet.spigot.authentication.result.AuthenticationResult;
import dev.dejvokep.safenet.spigot.authentication.result.HandshakeAuthenticationResult;
import dev.dejvokep.safenet.spigot.disconnect.DisconnectHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Listens for the {@link PacketType.Handshake.Client#SET_PROTOCOL} packet. This packet is the first one sent from the
 * client and is then used to read the <code>host</code> string and extract the properties.
 */
public class NativeHandshakeListener extends AbstractHandshakeListener {

    /**
     * Registers the handshake packet listener and handles the incoming connections.
     *
     * @param plugin the plugin
     */
    public NativeHandshakeListener(@NotNull SafeNetSpigot plugin) {
        super(plugin);
        // Reload
        reload();
        // Authenticator
        final Authenticator authenticator = plugin.getAuthenticator();

        // Listen to the handshake packet
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Handshake.Client.SET_PROTOCOL) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    // If malformed
                    if (event.getPacket().getProtocols().size() == 0 || event.getPacket().getStrings().size() == 0) {
                        logAuthResult(new HandshakeAuthenticationResult(AuthenticationResult.HANDSHAKE_MALFORMED_DATA));
                        disconnect(event);
                        return;
                    }

                    // If pinging and it is allowed
                    if (event.getPacket().getProtocols().read(0) == PacketType.Protocol.STATUS) {
                        if (isBlockPings())
                            disconnect(event);
                        return;
                    }

                    // Data
                    StructureModifier<String> strings = event.getPacket().getStrings();
                    String data = strings.readSafely(0);
                    // Authenticate
                    HandshakeAuthenticationResult result = authenticator.handshake(data);

                    // If failed
                    if (!result.getResult().isSuccess()) {
                        logAuthResult(result);
                        disconnect(event);
                        return;
                    }

                    // Set the host
                    strings.write(0, result.getData());
                } catch (Exception ex) {
                    logAuthException(ex);
                    disconnect(event);
                }
            }
        });
    }

    /**
     * Disconnects the invoker of the given event.
     *
     * @param event the packet event
     * @see DisconnectHandler#login(Player)
     */
    private void disconnect(@NotNull PacketEvent event) {
        try {
            getPlugin().getDisconnectHandler().login(event.getPlayer());
        } catch (Exception ex) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to disconnect a player! Shutting down...", ex);
            Bukkit.shutdown();
        }
    }

}