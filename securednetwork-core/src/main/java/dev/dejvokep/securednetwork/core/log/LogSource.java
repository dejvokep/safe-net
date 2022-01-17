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
package dev.dejvokep.securednetwork.core.log;

import dev.dejvokep.securednetwork.core.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enum representing sources from which a log message can come.
 */
public enum LogSource {

    /**
     * Authenticator messages.
     */
    AUTHENTICATOR("[AUTHENTICATOR] "),
    /**
     * Connector messages.
     */
    CONNECTOR("[CONNECTOR] "),
    /**
     * General messages.
     */
    GENERAL(""),
    /**
     * Updater messages.
     */
    UPDATER("[UPDATER] "),
    /**
     * IP-whitelist messages.
     */
    WHITELIST("[WHITELIST] ");

    // Prefix
    private final String prefix;

    /**
     * Initializes source's message prefix.
     *
     * @param prefix the prefix used in log records
     */
    LogSource(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the message prefix of the log source represented by this enum.
     *
     * @return the message prefix of the log source
     */
    public String getPrefix() {
        return prefix;
    }

}
