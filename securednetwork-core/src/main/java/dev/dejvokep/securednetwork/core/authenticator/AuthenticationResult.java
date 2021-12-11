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
package dev.dejvokep.securednetwork.core.authenticator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the authentication result created by {@link Authenticator}.
 * <p>
 * If the authentication failed, the string returned by {@link #getHost()} is always only the first element of the array
 * got from the original host string. This guarantees, that even if the socket closing fails, the server code will close
 * it anyway due to insufficient array length.
 */
public class AuthenticationResult {

    // Host to set back into the packet
    private final String host;
    // If passed
    private final boolean passed;

    // Player's UUID, or ?
    private final String playerId;

    /**
     * Initializes the authentication result by the given host string and if the authentication passed.
     *
     * @param host     the host string that excludes the property with the passphrase
     * @param passed   if the authentication passed
     * @param playerId the player's UUID, or <code>?</code> if unknown
     */
    AuthenticationResult(@NotNull String host, boolean passed, @Nullable String playerId) {
        this.host = host;
        this.passed = passed;
        this.playerId = playerId;
    }

    /**
     * Returns the host string without the property which includes the passphrase. This property should be set back into
     * the packet's <code>host</code> field.
     *
     * @return the host string that excludes the property with the passphrase
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns if the authentication passed (e.g. if the connection should be accepted).
     *
     * @return if the authentication passed
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Returns the UUID of the player who initiated the authentication request. If the UUID is unknown, returns
     * <code>?</code>.
     *
     * @return the UUID of the player who initiated the authentication request, or <code>?</code> if unknown
     */
    public String getPlayerId() {
        return playerId;
    }
}
