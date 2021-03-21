package com.davidcubesvk.securedNetworkProxy.ipWhitelist;

import java.util.regex.Pattern;

/**
 * Stores a whitelisted IP (IPv4 format) and covers basic manipulation methods.
 */
public class IPHolder {

    /**
     * IP (IPv4) address pattern.
     */
    private static final Pattern IP_PATTERN = Pattern.compile("((?:[^.]+\\.)+.+:[0-9]{1,5})|(\\[(?:[^.]+\\.)+.+:[0-9]{1,5}])");

    /**
     * The case-insensitive pattern.
     */
    private static final Pattern CASE_INSENSITIVE_PATTERN = Pattern.compile("\\[.+]");

    /**
     * The port colon.
     */
    public static final char PORT_COLON = ':';

    //The IP
    private String ip;
    //If to compare case-sensitively
    private boolean caseSensitive;

    /**
     * Compares the IP held with the given IP and returns whether they are the same.
     *
     * @param ip the IP to compare including the port in format <code>IP:port</code>
     * @return whether the given IP equals the held IP (if they are equal and the player connecting with such IP is
     * allowed to connect to the server)
     */
    public boolean compare(String ip) {
        //Return if equal
        return caseSensitive ? this.ip.equals(ip) : this.ip.equalsIgnoreCase(ip);
    }

    /**
     * Sets the IP held by this instance to the specified one (may include the case-insensitivity surrounding, must
     * include a port separated by {@link #PORT_COLON}). Returns if the operation was successful - if the IP is
     * correctly specified (matches the {@link #IP_PATTERN} pattern).
     *
     * @param ip the IP to be held
     * @return if the IP is valid and was successfully set
     */
    public boolean setIp(String ip) {
        //Replace all the spaces
        ip = ip.replace(" ", "");
        //Match the IP pattern
        if (!IP_PATTERN.matcher(ip).matches())
            return false;

        //Set the case-insensitive depending on the pattern match
        caseSensitive = !CASE_INSENSITIVE_PATTERN.matcher(ip).matches();
        //Set
        this.ip = ip;
        return true;
    }

    /**
     * Returns the IP held by this instance of this class.
     *
     * @return the IP held
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns if this IP is case sensitive.
     *
     * @return if this IP is case sensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
}
