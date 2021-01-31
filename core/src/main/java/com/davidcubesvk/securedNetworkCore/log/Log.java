package com.davidcubesvk.securedNetworkCore.log;

import com.davidcubesvk.securedNetworkCore.config.Config;

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
     *     <li><code>API</code>: API messages</li>
     *     <li><code>AUTHENTICATOR</code>: authenticator messages</li>
     *     <li><code>CONNECTOR</code>: connector messages</li>
     *     <li><code>GENERAL</code>: general messages</li>
     *     <li><code>UPDATER</code>: updater messages</li>
     *     <li><code>WHITELIST</code>: IP-whitelist messages</li>
     * </ul>
     */
    public enum Source {
        API("[API] "), AUTHENTICATOR("[AUTHENTICATOR] "), CONNECTOR("[CONNECTOR] "),
        GENERAL(""), UPDATER("[UPDATER] "), WHITELIST("[WHITELIST] ");

        //Prefix in messages
        String prefix;

        /**
         * Initializes source's message prefix.
         *
         * @param prefix the prefix used in log records
         */
        Source(String prefix) {
            //Set the prefix
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

    //File logger
    private final Logger fileLogger = Logger.getLogger("com.davidcubesvk.securedNetworkCore");
    //Plugin logger
    private final Logger pluginLogger;

    //If logging is enabled
    private final boolean enabled;

    /**
     * Loads the internal data.
     *
     * @param pluginLogger the plugin logger instance
     */
    public Log(Logger pluginLogger, File folder, Config config) {
        //Set the plugin logger
        this.pluginLogger = pluginLogger;

        //If enabled
        enabled = config.getBoolean("log");
        //If not enabled
        if (!enabled)
            return;

        //Make directory
        if (!folder.exists())
            folder.mkdir();
        //Set level
        fileLogger.setLevel(java.util.logging.Level.INFO);
        //Disable parent handlers
        fileLogger.setUseParentHandlers(false);

        //Load formatter
        Formatter formatter = new FileFormatter();
        //Create handler
        try {
            FileHandler fileHandler = new FileHandler(folder.getAbsolutePath() + "/" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".log");
            //Set formatter
            fileHandler.setFormatter(formatter);
            //Add handler
            fileLogger.addHandler(fileHandler);

            //Display notice
            log(Level.INFO, Log.Source.GENERAL, "-------------------------------- IMPORTANT! --------------------------------");
            log(Level.INFO, Log.Source.GENERAL, "Log files may contain sensitive information such as server IP, player names,");
            log(Level.INFO, Log.Source.GENERAL, "player UUIDs, etc. If sharing, please make sure you delete ALL of these information.");
            log(Level.INFO, Log.Source.GENERAL, "Logs should be shared only with people you trust.");
            log(Level.INFO, Log.Source.GENERAL, "----------------------------------------------------------------------------");
        } catch (IOException ex) {
            //Print into the console
            pluginLogger.log(Level.SEVERE, "An error occurred while initializing the logger!", ex);
        }
    }

    /**
     * Logs a message at the given level.
     *
     * @param level     the level of this message
     * @param source the log source of this message
     * @param message   a message to log
     * @see Logger#log(java.util.logging.Level, String)
     */
    public void log(Level level, Source source, String message) {
        if (enabled)
            fileLogger.log(level, source.getPrefix() + message);
    }

    /**
     * Logs a message with a throwable at the given level.
     *
     * @param level     the level of this message
     * @param source the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     * @see Logger#log(java.util.logging.Level, String, Throwable)
     */
    public void log(Level level, Source source, String message, Throwable throwable) {
        if (enabled)
            fileLogger.log(level, source.getPrefix() + message, throwable);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message at the given level.
     *
     * @param level     the level of this message
     * @param source the log source of this message
     * @param message   a message to log
     */
    public void logConsole(Level level, Source source, String message) {
        //Log
        if (enabled)
            log(level, source, message);

        //Print into the console
        pluginLogger.log(level, message);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message with a throwable at the given level.
     *
     * @param level     the level of this message
     * @param source the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     */
    public void logConsole(Level level, Source source, String message, Throwable throwable) {
        //Log
        if (enabled)
            log(level, source, message, throwable);

        //Print into the console
        pluginLogger.log(level, message, throwable);
    }

    /**
     * Logs (into the log file) and prints (into the console) a message with a throwable at the given level.<br>
     * The throwable is not printed into the console.
     *
     * @param level     the level of this message
     * @param source the log source of this message
     * @param message   a message to log
     * @param throwable a throwable to log
     */
    public void logConsoleWithoutThrowable(Level level, Source source, String message, Throwable throwable) {
        //Log
        if (enabled)
            log(level, source, message, throwable);

        //Print into the console
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
