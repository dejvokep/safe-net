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
package dev.dejvokep.safenet.bungeecord.updater;

import dev.dejvokep.safenet.bungeecord.SafeNetBungeeCord;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Updater class which checks for updates of the plugin by using a website API ({@link #URL}). If enabled, checks for
 * updates per the set delay.
 */
public class Updater {

    // URL to get latest version
    public static final String URL = "https://api.spigotmc.org/legacy/update.php?resource=65075";

    /**
     * Recheck delay in minutes.
     */
    private static final long RECHECK_DELAY = 180L;

    /**
     * Starts the updater (and it's repeating task repeatedly checking for a new update).
     *
     * @param plugin the plugin instance
     */
    public static void watch(@NotNull SafeNetBungeeCord plugin) {
        // Version
        String version = plugin.getDescription().getVersion();
        int dash = version.indexOf('-');
        if (dash != -1)
            version = version.substring(0, dash);
        // ID
        final int versionId = Integer.parseInt(version.replace(".", ""));

        // Schedule
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            // The latest version
            String latest;
            // Read
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(URL).openStream()))) {
                latest = reader.readLine();
            } catch (IOException ignored) {
                return;
            }

            // New version available
            if (Integer.parseInt(latest.replace(".", "")) > versionId)
                plugin.getLogger().warning("New version " + latest + " is available! Please update as soon as possible to receive the newest features and important security patches. You are using version " + plugin.getDescription().getVersion() + ".");
        }, 0L, RECHECK_DELAY, TimeUnit.MINUTES);
    }

}