/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.safenet.spigot.listener;

import dev.dejvokep.safenet.spigot.SafeNetSpigot;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;

/**
 * A utility class used to push listeners to be run as the first ones in an event cycle.
 */
public class ListenerPusher {

    // Plugin
    private final SafeNetSpigot plugin;
    // Handlers field
    private Field handlersField;

    public ListenerPusher(SafeNetSpigot plugin) {
        // Set
        this.plugin = plugin;

        // Obtain the field
        try {
            handlersField = HandlerList.class.getDeclaredField("handlerslots");
            handlersField.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst obtaining reflection components to push the session listener to the first place! This might cause passphrase leaks if another plugins handle the exposed data incorrectly! Shutting down...");
            Bukkit.shutdown();
        }
    }

    /**
     * Pushes the given listener to the first handler place for the handler list.
     *
     * @param list     the event handler list
     * @param priority priority to push in
     * @param listener listener to push
     */
    public void push(@NotNull HandlerList list, @NotNull EventPriority priority, @NotNull Listener listener) {
        // Unavailable
        if (handlersField == null)
            return;

        try {
            // Listeners on the lowest priority
            @SuppressWarnings("unchecked")
            ArrayList<RegisteredListener> listeners = ((EnumMap<EventPriority, ArrayList<RegisteredListener>>) handlersField.get(list)).get(priority);

            // Find the index of this listener
            int target = 0;
            for (; target < listeners.size(); target++) {
                if (listeners.get(target).getListener() == listener)
                    break;
            }

            // Nothing to do
            if (target == 0)
                return;

            // Move all listeners one place behind
            RegisteredListener push = listeners.get(target);
            while (--target >= 0)
                listeners.set(target + 1, listeners.get(target));
            // Set this listener as first
            listeners.set(0, push);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst pushing the session listener to the first place! This might cause passphrase leaks if another plugins handle the exposed data incorrectly! Shutting down...", ex);
            Bukkit.shutdown();
        }
    }

}