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

/**
 * An enum representing all authentication results produced by {@link Authenticator}.
 */
public enum AuthenticationResult {

    /**
     * Authentication (handshake and session) was successful.
     */
    SUCCESS(true, "", 0),

    /**
     * Handshake authentication failed due to no passphrase configured.
     * <p>
     * Happens when there's no passphrase, or it's empty.
     */
    HANDSHAKE_PASSPHRASE_NOT_CONFIGURED(false, "passphrase is not configured.", 1),
    /**
     * Handshake authentication failed due to insufficient length of the host data.
     * <p>
     * Usually happens when IP-forwarding is disabled.
     */
    HANDSHAKE_INSUFFICIENT_DATA_LENGTH(false, "data has insufficient length. Is ip-forward in BungeeCord config set to true?", 2),
    /**
     * Handshake authentication failed due to there being no properties available.
     */
    HANDSHAKE_NO_PROPERTIES(false, "no properties found.", 3),
    /**
     * Handshake authentication failed due the passphrase not being found.
     * <p>
     * Usually happens when there are any plugin interfering.
     */
    HANDSHAKE_PROPERTY_NOT_FOUND(false, "property not found. If that was you and you believe this is an error, please try checking for incompatible plugins installed.", 4),
    /**
     * Handshake authentication failed because the property was found, but the passphrase is invalid.
     * <p>
     * <b>This is unauthorized access most probably made by hackers.</b>
     */
    HANDSHAKE_INVALID_PASSPHRASE(false, "invalid passphrase. Someone might be currently trying to guess the passphrase.", 5),
    /**
     * Handshake authentication failed due to malformed data.
     */
    HANDSHAKE_MALFORMED_DATA(false, "data is malformed.", 6),


    /**
     * Session authentication failed due to reflection components being unavailable.
     */
    SESSION_REFLECTION_UNAVAILABLE(false, "reflection components are unavailable. Look for any errors during server startup.", 7),
    /**
     * Session authentication failed because there is no game profile.
     */
    SESSION_NO_GAME_PROFILE(false, "no game profile found.", 8),
    /**
     * Session authentication failed because there are no properties.
     */
    SESSION_NO_PROPERTIES(false, "no properties found.", 9),
    /**
     * Session authentication failed due to unexpected properties found with the same ID as the plugin's.
     */
    SESSION_UNEXPECTED_PROPERTIES(false, "unexpected properties found.", 10),
    /**
     * Session authentication failed due to the property not being found.
     */
    SESSION_PROPERTY_NOT_FOUND(false, "property not found.", 11),
    /**
     * Session authentication failed because the property was found, but the session key is invalid.
     * <p>
     * <b>This is unauthorized access most probably made by hackers.</b>
     */
    SESSION_INVALID(false, "invalid session key. Someone might be currently trying to guess the session key.", 12),

    /**
     * Authentication failed due to an unknown error.
     */
    UNKNOWN_ERROR(false, "an unknown error occurred.", 13);

    // Success
    private final boolean success;
    // Message
    private final String message;
    // Result code
    private final int code;

    /**
     * Initializes the result.
     *
     * @param success if successful
     * @param message message suffix to display
     * @param code    unique result code
     */
    AuthenticationResult(boolean success, @NotNull String message, int code) {
        this.success = success;
        this.message = message;
        this.code = code;
    }

    /**
     * Returns if this result represents a successful authentication.
     *
     * @return if the authentication was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message representation (suffix).
     *
     * @return the message
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Returns the unique code describing the result.
     *
     * @return the unique code
     */
    public int getCode() {
        return code;
    }

}