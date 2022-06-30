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
package dev.dejvokep.safenet.spigot.authentication.result;

import dev.dejvokep.safenet.spigot.authentication.Authenticator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Stores the authentication request created by {@link Authenticator}.
 */
public class HandshakeAuthenticationResult {

    // Data
    private final String data, serverHostname, socketAddressHostname, properties;
    private final UUID uniqueId;
    // Result
    private final AuthenticationResult result;

    /**
     * Initializes the authentication result. All data will be set to {@link Authenticator#UNKNOWN_DATA}.
     *
     * @param result the result
     */
    public HandshakeAuthenticationResult(@NotNull AuthenticationResult result) {
        this(Authenticator.UNKNOWN_DATA, result);
    }

    /**
     * Initializes the authentication result with the given data.
     *
     * @param data   the host string that excludes the passphrase property (to be set back to the packet)
     * @param result the result
     */
    public HandshakeAuthenticationResult(@NotNull String data, @NotNull AuthenticationResult result) {
        this(data, Authenticator.UNKNOWN_DATA, Authenticator.UNKNOWN_DATA, null, Authenticator.UNKNOWN_DATA, result);
    }

    /**
     * Initializes the authentication result with the given data. No parameter can be unknown when the result is {@link
     * AuthenticationResult#SUCCESS success}.
     *
     * @param data                  host string that excludes the passphrase property (to be set back to the packet)
     * @param serverHostname        server hostname (or {@link Authenticator#UNKNOWN_DATA unknown})
     * @param socketAddressHostname socket address hostname (or {@link Authenticator#UNKNOWN_DATA unknown})
     * @param uniqueId              player's unique ID (or <code>null</code> if unknown)
     * @param properties            properties (or {@link Authenticator#UNKNOWN_DATA unknown})
     * @param result                the result
     */
    public HandshakeAuthenticationResult(@NotNull String data, @NotNull String serverHostname, @NotNull String socketAddressHostname, @Nullable UUID uniqueId, @NotNull String properties, @NotNull AuthenticationResult result) {
        this.data = data;
        this.serverHostname = serverHostname;
        this.socketAddressHostname = socketAddressHostname;
        this.uniqueId = uniqueId;
        this.properties = properties;
        this.result = result;
    }

    /**
     * Returns the host string without the passphrase property. This property must be set back to the packet.
     *
     * @return the host string that excludes the passphrase property
     */
    @NotNull
    public String getData() {
        return data;
    }

    /**
     * Returns the authentication result.
     *
     * @return the result
     */
    @NotNull
    public AuthenticationResult getResult() {
        return result;
    }

    /**
     * Returns the server hostname of the handshake, or {@link Authenticator#UNKNOWN_DATA unknown} (only if the {@link
     * #getResult() result} is not {@link AuthenticationResult#SUCCESS success}).
     *
     * @return the server hostname of the handshake
     */
    @NotNull
    public String getServerHostname() {
        return serverHostname;
    }

    /**
     * Returns the socket address hostname of the handshake, or {@link Authenticator#UNKNOWN_DATA unknown} (only if the
     * {@link #getResult() result} is not {@link AuthenticationResult#SUCCESS success}).
     *
     * @return the socket address hostname of the handshake
     */
    @NotNull
    public String getSocketAddressHostname() {
        return socketAddressHostname;
    }

    /**
     * Returns the {@link java.util.UUID UUID} of the handshake, or <code>null</code> if unknown (only if the {@link
     * #getResult() result} is not {@link AuthenticationResult#SUCCESS success}).
     *
     * @return the unique ID of the handshake
     */
    @Nullable
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns the {@link java.util.UUID UUID} of the handshake, or {@link Authenticator#UNKNOWN_DATA unknown} (only if
     * the {@link #getResult() result} is not {@link AuthenticationResult#SUCCESS success}).
     *
     * @return the unique ID of the handshake
     */
    @NotNull
    public String getUniqueIdAsString() {
        return uniqueId == null ? Authenticator.UNKNOWN_DATA : uniqueId.toString();
    }

    /**
     * Returns the properties of the handshake, or {@link Authenticator#UNKNOWN_DATA unknown} (only if the {@link
     * #getResult() result} is not {@link AuthenticationResult#SUCCESS success}).
     *
     * @return the properties of the handshake
     */
    @NotNull
    public String getProperties() {
        return properties;
    }
}
