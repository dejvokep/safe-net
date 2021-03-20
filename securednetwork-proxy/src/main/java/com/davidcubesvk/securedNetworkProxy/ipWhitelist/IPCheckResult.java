package com.davidcubesvk.securedNetworkProxy.ipWhitelist;

import net.md_5.bungee.api.chat.TextComponent;

public class IPCheckResult {

    //If the IP check was passed
    private boolean passed;
    //The disconnect message
    private TextComponent message;

    /**
     * Initializes the result with the given state, without any message.
     *
     * @param passed if the check was passed
     */
    IPCheckResult(boolean passed) {
        this(passed, null);
    }

    /**
     * Initializes the result with the given state and message.
     *
     * @param passed  if the check was passed
     * @param message the disconnect message if not passed, or <code>null</code>
     */
    IPCheckResult(boolean passed, TextComponent message) {
        this.passed = passed;
        this.message = message;
    }

    /**
     * Returns whether the check was passed.
     *
     * @return if the check was passed
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Returns the disconnect message or <code>null</code> if {@link #isPassed()} returns <code>true</code>.
     *
     * @return the disconnect message or <code>null</code>
     */
    public TextComponent getMessage() {
        return message;
    }
}