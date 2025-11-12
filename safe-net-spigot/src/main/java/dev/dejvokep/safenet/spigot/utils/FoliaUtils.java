/*
 * Copyright 2025 https://dejvokep.dev/
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
package dev.dejvokep.safenet.spigot.utils;

import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Utility class for Folia-safe task scheduling.
 */
public class FoliaUtils {

    private static final boolean IS_FOLIA = classExists("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Run a task safely for a player.
     *
     * @param plugin the plugin instance
     * @param player the player
     * @param task   the task to run
     */
    public static void runPlayerSafe(JavaPlugin plugin, Player player, Runnable task) {
        if (IS_FOLIA) {
            player.getScheduler().execute(plugin, task, null, 0);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task safely in a region.
     *
     * @param plugin the plugin instance
     * @param world  the world
     * @param loc    the location
     * @param task   the task to run
     */
    public static void runRegionSafe(JavaPlugin plugin, World world, Location loc, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, loc, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task asynchronously with a callback.
     *
     * @param task     the async task
     * @param callback the callback to run after
     */
    public static void runAsync(Runnable task, Runnable callback) {
        CompletableFuture.runAsync(task).thenRun(callback);
    }

    /**
     * Run a delayed task safely (global if Folia).
     *
     * @param plugin the plugin instance
     * @param task   the task to run
     * @param delay  the delay in ticks
     */
    public static void runDelayedSafe(JavaPlugin plugin, Runnable task, long delay) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, ignored -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Check if the server is running Folia.
     *
     * @return true if Folia, false otherwise
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }
}