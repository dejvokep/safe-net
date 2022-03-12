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
        PASSED(true, "", 0),

        /**
         * Authentication failed due to no passphrase configured.
         * <p>
         * Happens when there's no passphrase, or it's empty.
         */
        FAILED_PASSPHRASE_NOT_CONFIGURED(false, "passphrase is not configured.", 1),

        /**
         * Authentication failed due to insufficient length of the host data.
         * <p>
         * Usually happens when IP-forwarding is disabled.
         */
        FAILED_INSUFFICIENT_LENGTH(false, "data has insufficient length. Is ip-forward in BungeeCord config set to true?", 2),

        /**
         * Authentication failed due to there being no properties available.
         */
        FAILED_NO_PROPERTIES(false, "no properties found.", 3),

        /**
         * Authentication failed due the passphrase not being found.
         * <p>
         * Usually happens when there are any plugin interfering.
         */
        FAILED_PASSPHRASE_NOT_FOUND(false, "passphrase not found. If that was you and you believe this is an error, please try checking for incompatible plugins installed.", 4),

        /**
         * Authentication failed due to malformed data.
         */
        FAILED_MALFORMED_DATA(false, "data is malformed.", 5),

        /**
         * Authentication failed due to an unknown error.
         */
        FAILED_UNKNOWN_ERROR(false, "an unknown error occurred.", 6);

        // If passed
        private final boolean passed;
        // Message
        private final String message;
        // Result code
        private final int code;

        /**
         * Initializes the result.
         *
         * @param passed  if passed
         * @param message simple message to display
         * @param code    unique result code
         */
        Result(boolean passed, String message, int code) {
            this.passed = passed;
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

    // Host and UUID
    private final String host, shortenedHost, playerId;
    // Result
    private final Result result;

    /**
     * Initializes the authentication result by the given data.
     *
     * @param host          the host string that excludes the property with the passphrase
     * @param playerId      the player's {@link java.util.UUID}, or {@link Authenticator#UNKNOWN_DATA} if
     *                      unknown
     * @param result        the result
     * @param shortenedHost shortened host string (<code>host</code> parameter with replaced textures' value and
     *                      signature)
     */
    AuthenticationRequest(@NotNull String host, @Nullable String playerId, @NotNull Result result, @NotNull String shortenedHost) {
        this.host = host;
        this.result = result;
        this.playerId = playerId;
        this.shortenedHost = shortenedHost;
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
     * Returns the shortened version of {@link #getHost()}, which has value and signature of the textures' property
     * replaced. <b>Used only for logging.</b>
     *
     * @return the shortened host
     */
    public String getShortenedHost() {
        return shortenedHost;
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
