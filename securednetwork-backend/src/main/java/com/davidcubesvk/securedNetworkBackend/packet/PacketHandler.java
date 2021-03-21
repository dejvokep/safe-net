package com.davidcubesvk.securedNetworkBackend.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.davidcubesvk.securedNetworkBackend.SecuredNetworkBackend;
import com.davidcubesvk.securedNetworkCore.authenticator.AuthenticationResult;
import com.davidcubesvk.securedNetworkCore.authenticator.Authenticator;
import com.davidcubesvk.securedNetworkCore.log.Log;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * Listens for the {@link PacketType.Handshake.Client#SET_PROTOCOL} packet. This packet is then used to read the
 * <code>host</code> string and extract the property passed by the proxy server from it. If there is the correct
 * property and value, connection is allowed. The property is then removed from the packet to hide it from unwanted
 * exposures. This is also why this packet was chosen - it is the first packet sent between the server and client,
 * so we can remove the property as soon as possible.<br>
 * <p></p>
 * Credit for some parts of this class goes to project BungeeGuard (https://github.com/lucko/BungeeGuard) and
 * it's contributors.
 */
public class PacketHandler {

    //Protocol manager
    private final ProtocolManager protocolManager;
    //The plugin instance
    private final SecuredNetworkBackend plugin;

    //If to block pings
    private boolean blockPings;

    /**
     * Registers the packet listener and handles the incoming connections.
     *
     * @param protocolManager the protocol manager used to register the listener and send packets
     * @param plugin          the main class
     */
    public PacketHandler(ProtocolManager protocolManager, SecuredNetworkBackend plugin) {
        //Set
        this.protocolManager = protocolManager;
        this.plugin = plugin;
        //Authenticator
        final Authenticator authenticator = plugin.getAuthenticator();

        //Listen to the handshake packet
        protocolManager.addPacketListener(new PacketAdapter(plugin.getPlugin(), PacketType.Handshake.Client.SET_PROTOCOL) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                //If pinging and it is allowed
                if (event.getPacket().getProtocols().read(0) == PacketType.Protocol.STATUS && !blockPings)
                    return;

                //Authenticate
                AuthenticationResult result = authenticator.authenticate(event.getPacket().getStrings().read(0));
                //Set the host
                event.getPacket().getStrings().write(0, result.getHost());
                //Log the result
                logResult(result.getPlayerId(), result.isPassed());
                //If failed
                if (!result.isPassed())
                    //Disconnect
                    if (!disconnect(event.getPlayer()))
                        //Mess up the hostname so the server will disconnect the player
                        event.getPacket().getStrings().write(0, "");
            }
        });
    }

    /**
     * Disconnects the given player. The player is an instance of
     * {@link com.comphenix.protocol.injector.server.TemporaryPlayer} now, which provides the socket instance, which is
     * used to close the connection. This is achieved by calling
     * {@link TemporaryPlayerFactory#getInjectorFromPlayer(Player)} and then
     * {@link com.comphenix.protocol.injector.server.SocketInjector#disconnect(String)}. Returns if disconnection was
     * successful.
     *
     * @param player the player to disconnect
     */
    private boolean disconnect(Player player) {
        try {
            //Message
            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getString("disconnect-failed-authentication"));

            //Create the disconnect packet
            PacketContainer disconnectPacket = new PacketContainer(PacketType.Login.Server.DISCONNECT);
            //Set the message
            disconnectPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(TextComponent.fromLegacyText(message))));
            //Send
            protocolManager.sendServerPacket(player, disconnectPacket);

            //Disconnect the player
            TemporaryPlayerFactory.getInjectorFromPlayer(player).disconnect(message);
            return true;
        } catch (InvocationTargetException ignored) {
            //If something happens, the server itself will disconnect the player due to insufficient host string length
            return false;
        }
    }

    /**
     * Reloads the internal configuration.
     */
    public void reload() {
        //If to block ping packets
        blockPings = plugin.getConfiguration().getBoolean("block-pings");
    }

    /**
     * Logs the result of a connection request determined by the authentication result. The cause is always
     * <code>failed_authentication</code> if the connection was rejected.
     *
     * @param playerId UUID of the player connecting, or <code>?</code> if unknown
     * @param accepted if the connection was accepted
     */
    private void logResult(String playerId, boolean accepted) {
        plugin.getLog().log(Level.INFO, Log.Source.CONNECTOR, "uuid=" + playerId + " result=" + (accepted ? "accepted" : "rejected") +
                (accepted ? "" : " cause=failed_authentication"));
    }

}