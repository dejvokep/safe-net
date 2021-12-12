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
package dev.dejvokep.securednetwork.bungeecord.updater;

import dev.dejvokep.securednetwork.bungeecord.SecuredNetworkBungeeCord;
import dev.dejvokep.securednetwork.core.log.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Updater class which checks for updates of the plugin by using a website API ({@link #URL}). If enabled, checks for
 * updates as per the set delay.
 */
public class Updater {

    // URL to get latest version
    public static final String URL = "https://api.spigotmc.org/legacy/update.php?resource=65075";
    // If is new version available and if to check that
    private boolean isNewVersion, enabled;
    // Recheck delay in minutes
    private int recheck;

    // Current version and the latest version of the plugin
    private final String currentVersion;
    private String latestVersion;

    // Updater task
    private ScheduledTask task;
    // The plugin instance
    private final SecuredNetworkBungeeCord plugin;

    /**
     * Starts the updater (and it's repeating task repeatedly checking for a new update).
     *
     * @param plugin the plugin instance
     */
    public Updater(@NotNull SecuredNetworkBungeeCord plugin) {
        // Set
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        // Reload
        reload();
    }

    /**
     * Reloads the internal data and schedules delayed update checking.
     */
    public void reload() {
        // If checking is enabled
        this.enabled = plugin.getConfiguration().getBoolean("updater.enabled");
        // If not enabled
        if (!enabled)
            return;

        // Get the refresh delay
        this.recheck = plugin.getConfiguration().getInt("updater.delay");
        // Refresh
        check();

        // If the task is not null
        if (task != null) {
            // Cancel
            task.cancel();
            // Set to null
            task = null;
        }

        // Rechecking for updates
        if (enabled && recheck != -1) {
            // Check if not invalid
            if (recheck < 1) {
                // Log that refresh rate is invalid
                plugin.getLog().log(Level.WARNING, Log.Source.UPDATER, "Recheck rate is smaller than 1min! Using value 1min.");
                recheck = 1;
            }

            // Recheck automatically
            task = ProxyServer.getInstance().getScheduler().schedule(plugin, this::check, recheck, recheck, TimeUnit.MINUTES);
        }
    }

    /**
     * Checks for a new update of the plugin and logs the result.
     * <p>
     * More specifically, reads the newest version from a website API, then announces if a new update is available or not.
     */
    private void check() {
        // If not enabled
        if (!enabled)
            return;

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            // Checking for updates
            plugin.getLog().logConsole(Level.INFO, Log.Source.UPDATER, "Checking for updates...");

            // Get current version of plugin
            int currentVersionNumbers = Integer.parseInt(this.currentVersion.replace(".", ""));

            // Get the latest version of plugin
            try {
                this.latestVersion = new BufferedReader(new InputStreamReader(new URL(URL).openStream())).readLine();
            } catch (IOException ex) {
                // Log the error
                plugin.getLog().logConsoleWithoutThrowable(Level.WARNING, Log.Source.UPDATER,
                        "Failed to check for updates.", ex);

                // Return
                return;
            }

            // Get the latest version in numbers
            int latestVersionNumbers = Integer.parseInt(this.latestVersion.replace(".", ""));
            this.isNewVersion = latestVersionNumbers > currentVersionNumbers;

            // Log and print to console
            if (isNewVersion)
                // New version available
                plugin.getLog().logConsole(Level.INFO, Log.Source.UPDATER, "New version " + latestVersion + " is available! You are using version " + currentVersion + ".");
        });
    }

    /**
     * Returns a join message to be sent to a player informing about plugin version status.
     *
     * @return the join message informing about plugin version status
     */
    public String getJoinMessage() {
        // Checking for updates is disabled
        if (!enabled) return "";

        // Replace and return
        return plugin.getConfiguration().getString("updater.message")
                .replace("{version_current}", currentVersion)
                .replace("{version_latest}", latestVersion);
    }

    /**
     * Returns if checking for updates is enabled.
     *
     * @return if checking for updates is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the latest version of the plugin in a string (x.x).
     *
     * @return the latest version
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Returns the current version of the plugin in a string (x.x).
     *
     * @return the current version
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Returns if a new version of the plugin is available.
     *
     * @return if a new version is available
     */
    public boolean isNewVersionAvailable() {
        return isNewVersion;
    }

}