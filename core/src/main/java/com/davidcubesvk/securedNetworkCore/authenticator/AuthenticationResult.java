package com.davidcubesvk.securedNetworkCore.authenticator;

/**
 * Stores the authentication result created by {@link Authenticator}.<br>
 * If the authentication failed, the string returned by {@link #getHost()} is always only the first element of the array
 * got from the original host string. This guarantees, that even if the socket closing fails, the server code will close
 * it anyway due to insufficient array length.
 */
public class AuthenticationResult {

    //Host string to set back into the packet
    private final String host;
    //If passed
    private final boolean passed;

    //Player's UUID, or ?
    private final String playerId;

    /**
     * Initializes the authentication result by the given host string and if the authentication passed.
     *
     * @param host   the host string that excludes the property with the passphrase
     * @param passed if the authentication passed
     * @param playerId the player's UUID, or <code>?</code> if unknown
     */
    AuthenticationResult(String host, boolean passed, String playerId) {
        this.host = host;
        this.passed = passed;
        this.playerId = playerId;
    }

    /**
     * Returns the host string without the property which includes the passphrase. This property should be set back into
     * the packet's <code>host</code> field.
     *
     * @return the host string that excludes the property with the passphrase
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns if the authentication passed (e.g. if the connection should be accepted).
     *
     * @return if the authentication passed
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Returns the UUID of the player who initiated the authentication request. If the UUID is unknown, returns
     * <code>?</code>.
     *
     * @return the UUID of the player who initiated the authentication request, or <code>?</code> if unknown
     */
    public String getPlayerId() {
        return playerId;
    }
}
