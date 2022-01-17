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
 * Stores the authentication request created by {@link Authenticator}.
 * <p>
 * If the authentication failed, the string returned by {@link #getHost()} is always only the first element of the array
 * got from the original host string. This guarantees, that even if the socket closing fails, the server code will close
 * it anyway due to insufficient array length.
 */
public class AuthenticationRequest {

    /**
     * Represents authentication result.
     */
    public enum Result {
        /**
         * Authentication was passed.
         */
        PASSED(true, "", "", 0),

        /**
         * Authentication failed due to insufficient length of the host data.
         * <p>
         * Usually happens when IP-forwarding is disabled.
         */
        FAIL_INSUFFICIENT_LENGTH(false, "insufficient_length", "host has insufficient length. Is IP-forward in BungeeCord config set to true?", 1),

        /**
         * Authentication failed due to there being no properties available.
         */
        FAIL_NO_PROPERTIES(false, "no_properties", "no properties found.", 2),

        /**
         * Authentication failed due the property not being found.
         * <p>
         * Usually happens when there are any plugin interfering.
         */
        FAIL_PROPERTY_NOT_FOUND(false, "property_not_found", "property not found. Is there any incompatible plugin installed?", 3);

        // If passed
        private final boolean passed;
        // String representation and message
        private final String string, message;
        // Result code
        private final int code;

        /**
         * Initializes the result.
         *
         * @param passed  if passed
         * @param string  the string representation
         * @param message simple message to display
         * @param code    unique result code
         */
        Result(boolean passed, String string, String message, int code) {
            this.passed = passed;
            this.string = string;
            this.message = message;
            this.code = code;
        }

        /**
         * Returns if this result represents a passed authentication.
         *
         * @return if this result represents a passed authentication
         */
        public boolean isPassed() {
            return passed;
        }

        /**
         * Returns as string.
         *
         * @return as string
         */
        public String getAsString() {
            return string;
        }

        /**
         * Returns the simple message representation.
         *
         * @return the simple message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the unique code.
         *
         * @return the unique code
         */
        public int getCode() {
            return code;
        }
    }

    // Host to set back into the packet
    private final String host;
    // Result
    private final Result result;

    // Player's UUID, or ?
    private final String playerId;

    /**
     * Initializes the authentication result by the given host string and if the authentication passed.
     *
     * @param host     the host string that excludes the property with the passphrase
     * @param playerId the player's UUID, or <code>?</code> if unknown
     * @param result   the result
     */
    AuthenticationRequest(@NotNull String host, @Nullable String playerId, @NotNull Result result) {
        this.host = host;
        this.result = result;
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
     * Returns the authentication result.
     *
     * @return the result.
     */
    public Result getResult() {
        return result;
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
