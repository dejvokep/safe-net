package com.davidcubesvk.securedNetworkProxy.ipWhitelist;

/**
 * Stores a whitelisted IP (IPv4 format) and covers basic manipulation methods.
 */
public class IPHolder {

    /**
     * The case-sensitive prefix.
     */
    private static final String PREFIX_CASE_SENSITIVE = "[CASE_SENSITIVE]";

    /**
     * The case-insensitive prefix.
     */
    private static final String PREFIX_CASE_INSENSITIVE = "[CASE_INSENSITIVE]";

    /**
     * The default value for case ignoring.
     */
    private static final boolean DEFAULT_CASE_IGNORING = false;

    /**
     * Separator used as split regex to split IP strings into parts.
     */
    public static final String PART_SEPARATOR = "\\.";

    /**
     * Colon which separates the port from the IP.
     */
    public static final char PORT_COLON = ':';

    /**
     * Wildcard character used as a replacement for strings of any length.
     */
    public static final String WILDCARD = "*";

    //The IP and port
    private String ip, port;
    //IP parts
    private String[] parts;
    //If to compare case-sensitively and if the port is a wildcard
    private boolean caseSensitive, wildcardPort;

    /**
     * Compares the IP held with the given IP and returns whether the given IP suits the specification of the held IP.
     * This depends on:
     * <ul>
     *     <li>if IP part amounts equal (if they have the same amount of parts got by splitting the IP using
     *     {@link #PART_SEPARATOR},</li>
     *     <li>if all parts equal (if the held IP is using a wildcard as some part, the part comparing is skipped).</li>
     * </ul>
     *
     * @param ipParts the IP to compare split using {@link #PART_SEPARATOR}
     * @return whether the given IP suits the specification of the help IP (if they equal and the player connecting with
     * such IP is allowed to connect to the server)
     */
    public boolean compare(String[] ipParts, String port) {
        //If the IP parts do not equal
        if (parts.length != ipParts.length)
            return false;
        //If the port is not a wildcard and they do not equal
        if (!wildcardPort && !this.port.equals(port))
            return false;

        //Go through all parts
        for (int index = 0; index < parts.length; index++) {
            //If using wildcard
            if (parts[index].equals(WILDCARD))
                continue;

            //If do not equal
            if (caseSensitive ? !parts[index].equals(ipParts[index]) : !parts[index].equalsIgnoreCase(ipParts[index]))
                return false;
        }

        //Equal
        return true;
    }

    /**
     * Sets the IP held by this instance to the specified one (may include the case-sensitivity prefix, must include
     * a port separated by {@link #PORT_COLON}). If the case-sensitivity prefix is not included, defaults to
     * case-sensitive. Returns if the operation was successful - if the IP is correctly specified.
     *
     * @param ip the IP to be held
     */
    public boolean setIp(String ip) {
        //Parse by the prefixes
        if (ip.startsWith(PREFIX_CASE_SENSITIVE)) {
            //Case-sensitive
            caseSensitive = true;
            //Cut the prefix out
            ip = ip.substring(PREFIX_CASE_SENSITIVE.length()).trim();
        } else if (ip.startsWith(PREFIX_CASE_INSENSITIVE)) {
            //Not case-sensitive
            caseSensitive = false;
            //Cut the prefix out
            ip = ip.substring(PREFIX_CASE_INSENSITIVE.length()).trim();
        } else {
            //Default
            caseSensitive = !DEFAULT_CASE_IGNORING;
        }

        //If the port is not specified
        if (ip.indexOf(IPHolder.PORT_COLON) == -1 || ip.endsWith(String.valueOf(IPHolder.PORT_COLON)))
            return false;

        //The port index
        int portIndex = ip.indexOf(PORT_COLON) + 1;
        //Set
        this.ip = ip.substring(0, portIndex - 1);
        this.port = ip.substring(portIndex);
        //Reset parts
        parts = this.ip.split(PART_SEPARATOR);
        //If the port is a wildcard
        wildcardPort = port.equals(WILDCARD);
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
