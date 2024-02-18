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
package dev.dejvokep.safenet.core;

import java.security.SecureRandom;

/**
 * A utility class used to generate random keys.
 */
public class KeyGenerator {

    /**
     * Characters (90 chars) used to generate keys.
     */
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-=[];,./~!@#$%^&*()_+{}|:<>?";

    /**
     * Generates a random key of the specified length from {@link #CHARS}. If <code>length < 1</code>, returns
     * <code>null</code>.
     *
     * @param length length of the key to generate
     * @return the generated key
     */
    public static String generate(int length) {
        // If the length is less than 1
        if (length < 1)
            throw new IllegalArgumentException("Length of the key to generate must be at least 1!");

        // Secure random
        SecureRandom random = new SecureRandom();
        // String builder
        StringBuilder stringBuilder = new StringBuilder();
        // Build the passphrase
        for (int count = 0; count < length; count++)
            // Append a new character
            stringBuilder.append(CHARS.charAt(random.nextInt(CHARS.length())));

        // Return
        return stringBuilder.toString();
    }

}