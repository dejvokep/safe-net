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
package dev.dejvokep.securednetwork.core.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Class responsible for logging selected connections.
 */
public class ConnectionLogger {

    // Callback
    private final Consumer<String> callback;
    // Identifier
    private String identifier;

    /**
     * Creates a logger with the given callback.
     *
     * @param callback the callback to call with the connection data when the logger is attached to the connection
     */
    public ConnectionLogger(@NotNull Consumer<String> callback) {
        this.callback = callback;
    }

    /**
     * Attaches the logger to the given identifier.
     *
     * @param identifier the identifier to attach to
     */
    public void attach(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * Detaches the logger.
     */
    public void detach() {
        this.identifier = null;
    }

    /**
     * Handles the given connection identifier and message.
     *
     * @param identifier the connection identifier
     * @param message    the message
     */
    public void handle(@Nullable String identifier, @NotNull String message) {
        // If detached or identifiers do not equal
        if (this.identifier == null || identifier == null || (!this.identifier.equals("all") && !this.identifier.equals(identifier)))
            return;

        // Call
        callback.accept(message);
        // Detach
        detach();
    }

}