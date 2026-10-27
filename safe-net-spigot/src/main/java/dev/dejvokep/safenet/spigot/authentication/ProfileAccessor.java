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
package dev.dejvokep.safenet.spigot.authentication;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * A utility class for {@link GameProfile} access across server versions.
 * <p>
 * The plugin is compiled against the modern authlib (Minecraft 1.21.9+), where {@link GameProfile} is a record with an
 * immutable property map, and calls that API directly. Older servers, where the profile is a class with {@code getX()}
 * accessors and a mutable property map, are supported through reflection. The modern methods are never invoked on older
 * servers, so they are never linked there.
 */
class ProfileAccessor {

    /**
     * Name of the server player superclass declaring the profile field.
     */
    private static final String PLAYER_CLASS = "net.minecraft.world.entity.player.Player";

    // If the modern (record) profile API is available
    private final boolean modern;
    // Legacy accessor (null on modern servers) + getHandle method (null on legacy servers)
    private final Method legacyPropertiesGetter, handleMethod;
    // Profile field of the server player, needed to replace immutable profiles (null on legacy servers or if not found)
    private final Field profileField;
    // Class and method necessary for profile manipulation
    private final Class<?> craftPlayerClass;
    private final Method profileMethod;

    /**
     * Looks up the necessary reflection components.
     *
     * @throws ReflectiveOperationException if neither the modern nor the legacy profile API could be found
     */
    ProfileAccessor() throws ReflectiveOperationException {
        modern = hasMethod("properties");

        craftPlayerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
        profileMethod = craftPlayerClass.getDeclaredMethod("getProfile");
        profileMethod.setAccessible(true);

        legacyPropertiesGetter = modern ? null : GameProfile.class.getMethod("getProperties");
        handleMethod = modern ? craftPlayerClass.getMethod("getHandle") : null;
        profileField = modern ? profileField(handleMethod.getReturnType()) : null;
    }

    /**
     * Returns the profile for the given player.
     *
     * @param player the player whose profile to return
     * @return the player's profile
     * @throws ReflectiveOperationException an invocation or access exception
     */
    @Nullable
    GameProfile getProfile(@NotNull Player player) throws ReflectiveOperationException {
        return (GameProfile) profileMethod.invoke(craftPlayerClass.cast(player));
    }

    /**
     * Returns the property map of the given profile.
     *
     * @param profile the profile
     * @return the property map
     * @throws ReflectiveOperationException an invocation or access exception (legacy servers only)
     */
    @Nullable
    PropertyMap getProperties(@NotNull GameProfile profile) throws ReflectiveOperationException {
        return modern ? profile.properties() : (PropertyMap) legacyPropertiesGetter.invoke(profile);
    }

    /**
     * Removes all properties with the given name from the profile.
     * <p>
     * If the property map is mutable (legacy servers), the properties are removed in place and the given profile is
     * returned. If it is immutable (modern servers), a copy of the profile without the properties is returned instead;
     * the caller is then responsible for putting it in place of the original.
     *
     * @param profile the profile
     * @param name    name of the properties to remove
     * @return the profile without the properties, which is the given instance if it was modified in place
     * @throws ReflectiveOperationException an invocation or access exception (legacy servers only)
     */
    @NotNull
    private GameProfile removeProperties(@NotNull GameProfile profile, @NotNull String name) throws ReflectiveOperationException {
        // Properties
        PropertyMap map = getProperties(profile);
        // Nothing to remove (also avoids touching an immutable map needlessly)
        if (map == null || !map.containsKey(name))
            return profile;

        // Legacy maps are mutable
        if (!modern) {
            map.removeAll(name);
            return profile;
        }

        // Modern maps are immutable, rebuild without the properties
        ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
        for (Map.Entry<String, Property> entry : map.entries())
            if (!entry.getKey().equals(name))
                builder.put(entry.getKey(), entry.getValue());

        return new GameProfile(profile.id(), profile.name(), new PropertyMap(builder.build()));
    }

    /**
     * Removes all properties with the given name from the player's profile. If the profile's property map is immutable
     * (Minecraft 1.21.9+), the profile is rebuilt without the properties and replaced in the server player.
     * <p>
     * Failing to strip the properties is logged, but does not fail the authentication: the passphrase is always removed
     * during handshake authentication, and the session key is only of use before the handshake listener is active.
     *
     * @param player  the player
     * @param profile the player's current profile
     * @param name    name of the properties to remove
     * @return the player's profile after the removal
     * @throws ReflectiveOperationException an invocation or access exception
     */
    @NotNull
    GameProfile stripProfile(@NotNull Player player, @NotNull GameProfile profile, @NotNull String name) throws ReflectiveOperationException {
        // Remove
        GameProfile updated = removeProperties(profile, name);
        // Modified in place or nothing to remove
        if (updated == profile)
            return profile;

        // Replace in Player -> CraftPlayer -> ServerPlayer implements Player
        profileField.set(handleMethod.invoke(craftPlayerClass.cast(player)), updated);
        return profile;
    }

    /**
     * Returns the profile field declared in the class hierarchy of the server player handle, as declared by the return
     * type of <code>CraftPlayer#getHandle()</code>. Class names differ between platforms (e.g. <code>EntityHuman</code> on
     * Spigot, <code>Player</code> on Paper and 26.1+), so the hierarchy is walked instead. In each class, the field is
     * looked up by its Mojang name (<code>gameProfile</code>), available on unobfuscated (26.1+) and Mojang-mapped
     * (Paper) servers; if not found (obfuscated Spigot servers), by the single instance field of the profile type.
     *
     * @param handleClass class of the server player handle
     * @return the field, or <code>null</code> if it could not be found
     * @throws ReflectiveOperationException an invocation or access exception
     */
    @NotNull
    private static Field profileField(@NotNull Class<?> handleClass) throws ReflectiveOperationException {
        // Walk the tree
        for (Class<?> clazz = handleClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {

            Field field;
            try {
                field = clazz.getDeclaredField("gameProfile");
                if (Modifier.isStatic(field.getModifiers()) || field.getType() != GameProfile.class)
                    field = null;
            } catch (NoSuchFieldException ignored) {
                field = null;
            }

            if (field == null) {
                // Find all the relevant fields
                for (Field candidate : clazz.getDeclaredFields()) {
                    // Must be an instance field of the profile type
                    if (Modifier.isStatic(candidate.getModifiers()) || candidate.getType() != GameProfile.class)
                        continue;
                    field = candidate;
                    break;
                }
            }

            // Make it accessible
            if (field != null) {
                field.setAccessible(true);
                return field;
            }
        }

        // None found
        throw new NoSuchFieldException("Could not find a field holding GameProfile!");
    }

    /**
     * Returns if {@link GameProfile} has a public method with the given name and no parameters.
     *
     * @param name name of the method
     * @return if the method exists
     */
    private static boolean hasMethod(@NotNull String name) {
        try {
            GameProfile.class.getMethod(name);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

}
