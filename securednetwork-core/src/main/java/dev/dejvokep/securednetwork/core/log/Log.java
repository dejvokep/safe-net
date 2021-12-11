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
 * Covers a {@link Logger} for the plugin's logging system.
 */
public class Log {

    /**
     * Enum representing sources from which a log message can come.
     * <ul>
     *     <li><code>AUTHENTICATOR</code>: authenticator messages</li>
     *     <li><code>CONNECTOR</code>: connector messages</li>
     *     <li><code>GENERAL</code>: general messages</li>
     *     <li><code>UPDATER</code>: updater messages</li>
     *     <li><code>WHITELIST</code>: IP-whitelist messages</li>
     * </ul>
     */
    public enum Source {
        AUTHENTICATOR("[AUTHENTICATOR] "), CONNECTOR("[CONNECTOR] "),
        GENERAL(""), UPDATER("[UPDATER] "), WHITELIST("[WHITELIST] ");

        // Prefix in messages
        String prefix;

        /**
         * Initializes source's message prefix.
         *
         * @param prefix the prefix used in log records
         */
        Source(String prefix) {
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

    // File logger
    private final Logger fileLogger = Logger.getLogger("dev.dejvokep.securednetwork.core");
    // Plugin logger
    private final Logger pluginLogger;

    // If logging is enabled
    private final boolean enabled;

    /**
     * Loads the internal data.
     *
     * @param pluginLogger the plugin logger instance
     */
    public Log(@NotNull Logger pluginLogger, @NotNull File folder, @NotNull Config config) {
        // Set the plugin logger
        this.pluginLogger = pluginLogger;

        // If enabled
        enabled = config.getBoolean("log");
        // If not enabled
        if (!enabled)
            return;

        // Make directory
        if (!folder.exists())
            folder.mkdirs();
        // Set level
        fileLogger.setLevel(Level.INFO);
        // Disable parent handlers
        fileLogger.setUseParentHandlers(false);

        // Load formatter
        Formatter formatter = new FileFormatter();
        // Create handler
        try {
            FileHandler fileHandler = new FileHandler(folder.getAbsolutePath() + "/" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".log");
            // Set formatter
            fileHandler.setFormatter(formatter);
            // Add handler
            fileLogger.addHandler(fileHandler);

            // Display notice
            log(Level.INFO, Log.Source.GENERAL, "-------------------------------- IMPORTANT! --------------------------------");
            log(Level.INFO, Log.Source.GENERAL, "Log files may contain sensitive information such as server IP, player names,");
            log(Level.INFO, Log.Source.GENERAL, "player UUIDs, etc. If sharing, please make sure you delete ALL of these information.");
            log(Level.INFO, Log.Source.GENERAL, "Logs should be shared only with people you trust.");
            log(Level.INFO, Log.Source.GENERAL, "----------------------------------------------------------------------------");
        } catch (IOException ex) {
            // Print into the console
            pluginLogger.log(Level.SEVERE, "An error occurred while initializing the logger!", ex);
        }
    }

    /**
     * Logs a message at the given level.
     *
     * @param level   the level of this message
     * @param source  the log source of this message
     * @param message a message to log
     * @see Logger#log(Level, String)
     */
    public void log(@NotNull Level level, @NotNull Source source, @Nullable String message) {
        if (enabled)
            fileLogger.log(level, source.getPrefix() + message);
    }

    /**
     * Logs a message with a throwable at the given level.
     *
     * @param level     the level of this message
     * @param source    the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     * @see Logger#log(Level, String, Throwable)
     */
    public void log(@NotNull Level level, @NotNull Source source, @Nullable String message, @NotNull Throwable throwable) {
        if (enabled)
            fileLogger.log(level, source.getPrefix() + message, throwable);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message at the given level.
     *
     * @param level   the level of this message
     * @param source  the log source of this message
     * @param message a message to log
     */
    public void logConsole(@NotNull Level level, @NotNull Source source, @Nullable String message) {
        // Log
        if (enabled)
            log(level, source, message);

        // Print into the console
        pluginLogger.log(level, message);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message with a throwable at the given level.
     *
     * @param level     the level of this message
     * @param source    the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     */
    public void logConsole(@NotNull Level level, @NotNull Source source, @Nullable String message, @NotNull Throwable throwable) {
        // Log
        if (enabled)
            log(level, source, message, throwable);

        // Print into the console
        pluginLogger.log(level, message, throwable);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message with a throwable at the given level.
     * <p>
     * The throwable is not printed into the console.
     *
     * @param level     the level of this message
     * @param source    the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     */
    public void logConsoleWithoutThrowable(@NotNull Level level, @NotNull Source source, @Nullable String message, @NotNull Throwable throwable) {
        // Log
        if (enabled)
            log(level, source, message, throwable);

        // Print into the console
        pluginLogger.log(level, message);
    }

    /**
     * Returns if logging is enabled.
     *
     * @return if logging is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

}
