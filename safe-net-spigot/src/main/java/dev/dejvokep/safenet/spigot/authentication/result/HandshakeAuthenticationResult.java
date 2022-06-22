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

/**
 * Stores the authentication request created by {@link Authenticator}.
 */
public class HandshakeAuthenticationResult {

    // Host and UUID
    private final String host, playerId;
    // Result
    private final AuthenticationResult result;

    /**
     * Initializes the authentication result with the given data.
     *
     * @param host     the host string that excludes the passphrase property (to be set back to the packet)
     * @param playerId the player's {@link java.util.UUID UUID}, or {@link Authenticator#UNKNOWN_DATA} if unknown
     * @param result   the result
     */
    public HandshakeAuthenticationResult(@NotNull String host, @Nullable String playerId, @NotNull AuthenticationResult result) {
        this.host = host;
        this.result = result;
        this.playerId = playerId;
    }

    /**
     * Returns the host string without the passphrase property. This property must be set back to the packet.
     *
     * @return the host string that excludes the passphrase property
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the authentication result.
     *
     * @return the result
     */
    public AuthenticationResult getResult() {
        return result;
    }

    /**
     * Returns the {@link java.util.UUID UUID} of the player who initiated the authentication. If it is unknown, returns
     * {@link Authenticator#UNKNOWN_DATA}.
     *
     * @return the unique ID of the player who initiated the authentication
     */
    public String getPlayerId() {
        return playerId;
    }
}
