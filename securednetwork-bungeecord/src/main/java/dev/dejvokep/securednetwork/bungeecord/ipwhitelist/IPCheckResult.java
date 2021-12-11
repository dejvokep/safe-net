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
package dev.dejvokep.securednetwork.bungeecord.ipwhitelist;

import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Represents result of IP check.
 */
public class IPCheckResult {

    //If the IP check was passed
    private final boolean passed;
    //The disconnect message
    private final TextComponent message;

    /**
     * Initializes the result with the given state, without any message.
     *
     * @param passed if the check was passed
     */
    IPCheckResult(boolean passed) {
        this(passed, null);
    }

    /**
     * Initializes the result with the given state and message.
     *
     * @param passed  if the check was passed
     * @param message the disconnect message if not passed, <code>null</code> otherwise
     */
    IPCheckResult(boolean passed, @Nullable TextComponent message) {
        this.passed = passed;
        this.message = message;
    }

    /**
     * Returns whether the check was passed.
     *
     * @return if the check was passed
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Returns the disconnect message, or <code>null</code> if {@link #isPassed()} returns <code>true</code>.
     *
     * @return the disconnect message or <code>null</code>
     */
    public TextComponent getMessage() {
        return message;
    }
}