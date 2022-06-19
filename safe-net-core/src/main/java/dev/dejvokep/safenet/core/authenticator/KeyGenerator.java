package dev.dejvokep.safenet.core.authenticator;

import java.security.SecureRandom;

/**
 * A utility class used to generate random keys for session authentication.
 */
public class KeyGenerator {

    /**
     * Characters (90 chars) used to generate keys.
     */
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-=[];,./~!@#$%^&*()_+{}|:<>?";

    /**
     * Generates a key of the specified length from {@link #CHARS}. If <code>length < 1</code>, returns
     * <code>null</code>.
     *
     * @param length length of the key to generate
     * @return the generated key
     */
    public static String generate(int length) {
        // If the length is less than 1
        if (length < 1)
            return null;

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