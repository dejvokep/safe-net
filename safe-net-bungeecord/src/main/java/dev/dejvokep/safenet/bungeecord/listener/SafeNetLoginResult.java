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
package dev.dejvokep.safenet.bungeecord.listener;

import dev.dejvokep.safenet.core.PassphraseStore;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Class extending {@link LoginResult} which securely passes the passphrase to server connector, if called by handler
 * boss (see {@link #SERVER_CONNECTOR_CLASS} and {@link #HANDLER_BOSS_CLASS}) and by respective methods of these classes
 * ({@link #SERVER_CONNECTOR_CONNECTED_METHOD} and {@link #HANDLER_BOSS_CHANNEL_ACTIVE_METHOD}).
 * <p>
 * If the server is in online-mode, values from the previous login result are used. If in offline-mode, the server does
 * not supply the login result. As this custom login result is there every time (not depending on the mode), the
 * returned values are <code>null</code> (replacing the <code>null</code> login profile), except the properties. The
 * returned properties are (if not called by methods mentioned above) always an empty array, as during the testing
 * phase, a lot of problems were found when using some plugins if the value was <code>null</code>.
 * <p>
 * <i>The use of custom login results to forward plugin data to backend servers was inspired by BungeeGuard.</i>
 */
public class SafeNetLoginResult extends LoginResult {

    /**
     * Server connector class.
     */
    public static final String SERVER_CONNECTOR_CLASS = "net.md_5.bungee.ServerConnector";
    /**
     * Connected method in server connector.
     */
    public static final String SERVER_CONNECTOR_CONNECTED_METHOD = "connected";
    /**
     * Handler boss class.
     */
    public static final String HANDLER_BOSS_CLASS = "net.md_5.bungee.netty.HandlerBoss";
    /**
     * Channel active method in handler boss.
     */
    public static final String HANDLER_BOSS_CHANNEL_ACTIVE_METHOD = "channelActive";

    // Property array with passphrase
    private Property[] withPassphrase;
    // Store
    private final PassphraseStore passphraseStore;

    /**
     * Copies references of variables from the login result obtained from a {@link net.md_5.bungee.api.event.LoginEvent}.
     * If the given result is <code>null</code>, all variables used by the result are set to <code>null</code>.
     *
     * @param fromLogin       the login result obtained from a {@link net.md_5.bungee.api.event.LoginEvent}
     * @param passphraseStore the store providing the secret passphrase and other needed data
     */
    SafeNetLoginResult(@Nullable LoginResult fromLogin, @NotNull PassphraseStore passphraseStore) {
        // Call as in offline mode
        super(null, null, new Property[0]);
        // Set
        this.passphraseStore = passphraseStore;

        // If the result from the event contains any data - online mode
        if (fromLogin != null) {
            // Set the ID
            setId(fromLogin.getId());
            // Set the player name
            setName(fromLogin.getName());
            // Set properties
            setProperties(fromLogin.getProperties());
        }

        // Reset the custom properties
        resetCustomProperties();
    }

    @Override
    public Property[] getProperties() {
        // The stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // If unable to verify
        if (stackTrace.length < 4)
            return super.getProperties();

        // Server connector caller
        StackTraceElement serverConnector = stackTrace[2];
        // Handler boss caller
        StackTraceElement handlerBoss = stackTrace[3];

        // Verify callers
        if (serverConnector.getClassName().equals(SERVER_CONNECTOR_CLASS) && serverConnector.getMethodName().equals(SERVER_CONNECTOR_CONNECTED_METHOD)
                && handlerBoss.getClassName().equals(HANDLER_BOSS_CLASS) && handlerBoss.getMethodName().equals(HANDLER_BOSS_CHANNEL_ACTIVE_METHOD))
            // Return properties with the passphrase
            return withPassphrase;
        else
            // Return normal properties
            return super.getProperties();
    }

    @Override
    public void setProperties(Property[] properties) {
        // Set the properties
        super.setProperties(properties);
        // Reset the custom properties
        resetCustomProperties();
    }

    /**
     * Resets the custom property array.
     */
    private void resetCustomProperties() {
        // Make a copy of the original properties, or create a new instance with a space for the passphrase
        withPassphrase = getProperties() == null ? new Property[1] : Arrays.copyOf(getProperties(), getProperties().length + 1);
        // Add the passphrase
        withPassphrase[withPassphrase.length - 1] = new Property(PassphraseStore.PASSPHRASE_PROPERTY_NAME, passphraseStore.getPassphrase(), "");
    }

}