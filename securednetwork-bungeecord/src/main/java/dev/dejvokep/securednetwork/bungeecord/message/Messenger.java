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
package dev.dejvokep.securednetwork.bungeecord.message;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Class used to send messages to a player.
 * <p>
 * When a line break in chat occurs, the next line is without any color. This class inserts the last color codes before
 * every word in a message, so everything is colored as it should be, even after a line break.
 */
public class Messenger {

    // Color characters
    public static final Collection<Character> COLOR_CHARS = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f');
    // Modifier characters
    public static final Collection<Character> MODIFIER_CHARS = Arrays.asList('K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'R', 'r');

    /**
     * Sends a message to a player.
     *
     * @param sender  the receiver
     * @param message the message to send
     */
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        // If the message is empty
        if (message.equals("")) return;

        // The first space in the message (possible line break)
        int spaceIndex = message.indexOf(' ');

        // If the message is one-word
        if (spaceIndex == -1)
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));

        // The final message, the currently processed word, the unprocessed part
        String finalMessage = "", word, rest = message;
        // Index of the last space
        int lastSpaceIndex = 0;

        // This is the first word
        boolean first = true;

        // Loop until all words are processed
        while (rest.indexOf(' ') != -1) {
            // If this is the first word
            if (first) {
                // Add to the final message
                finalMessage = rest.substring(0, rest.indexOf(' '));
                // Add the rest
                rest = rest.substring(rest.indexOf(' ') + 1);

                // Next run will not be the first
                first = false;
                continue;
            }

            // Get the space index
            spaceIndex = rest.indexOf(' ');
            // Current word
            word = rest.substring(lastSpaceIndex, spaceIndex);

            // Substring the rest
            rest = rest.substring(spaceIndex + 1);
            // Add the lastly used color before the word
            word = getLastColor(finalMessage, word) + word;

            // Add to the final message
            finalMessage = finalMessage + " " + word;
        }

        // Process the rest
        finalMessage = finalMessage + " " + getLastColor(finalMessage, rest) + rest;

        // Send the message
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', finalMessage)));
    }

    /**
     * Returns the last color codes (combination of color and modifier char if available, see {@link #COLOR_CHARS} and {@link #MODIFIER_CHARS}) used in the given string. If not found, returns an empty string.
     *
     * @param searched a string to be searched
     * @param next     a string that goes after the searched one (to check if there's a color code that will override the effect)
     * @return the last color codes used in the given string, or an empty string if not found
     */
    private String getLastColor(@NotNull String searched, @NotNull String next) {
        // If the next string is at least 2 chars long, and it begins with a color code
        if (next.length() >= 2 && next.charAt(0) == '&' && COLOR_CHARS.contains(next.charAt(1)))
            return "";

        // Color and modifier found
        String color = "", modifier = "";

        // Last index
        int lastIndex = searched.lastIndexOf('&');
        // Loop until the last index is -1 (occurrence not found)
        while (lastIndex >= 0) {
            // If it is the last char in the string
            if (searched.length() <= lastIndex + 1)
                continue;

            // Get the color/modifier code
            char code = searched.charAt(lastIndex + 1);
            // If this is a modifier code and the color char has not been found yet (color char clears the modifier effect)
            if (MODIFIER_CHARS.contains(code)) {
                modifier = "&" + code;
            } else if (COLOR_CHARS.contains(code)) {
                color = "&" + code;
                break;
            }

            // Change the last index
            lastIndex = searched.lastIndexOf('&');
        }

        // Return the last colors used
        return color + modifier;
    }

}