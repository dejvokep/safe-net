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
package dev.dejvokep.safenet.bungeecord.listener.result;

import dev.dejvokep.safenet.core.PassphraseVault;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Class extending {@link LoginResult} which securely passes the passphrase to server connector.
 * <p>
 * If the server is in online-mode, values from the previous login result are used. If in offline-mode, the server does
 * not supply the login result. As this custom login result is there every time (not depending on the mode), the
 * returned values are <code>null</code> (replacing the <code>null</code> login profile), except the properties. The
 * returned properties are (if not called by methods mentioned above) always an empty array, as during the testing
 * phase, a lot of problems were found when using some plugins if the value was <code>null</code>.
 * <p>
 * <i>The use of custom login results to forward plugin data to backend servers was inspired by BungeeGuard, although
 * was perfected to properly handle edge cases and wide selection of server software implementations.</i>
 */
public class SafeNetLoginResult extends LoginResult {

    // Property array with passphrase
    private Property[] withPassphrase;
    // Vault
    private final PassphraseVault passphraseVault;
    // Verifier
    private final StackTraceVerifier stackTraceVerifier;

    /**
     * Copies references of variables from the login result obtained from a
     * {@link net.md_5.bungee.api.event.LoginEvent}. If the given result is <code>null</code>, all variables used by the
     * result are set to <code>null</code>.
     *
     * @param fromLogin          the login result obtained from a {@link net.md_5.bungee.api.event.LoginEvent}
     * @param passphraseVault    the store providing the secret passphrase and other needed data
     * @param stackTraceVerifier the stack trace verifier
     */
    SafeNetLoginResult(@Nullable LoginResult fromLogin, @NotNull PassphraseVault passphraseVault, @NotNull StackTraceVerifier stackTraceVerifier) {
        // Call as in offline mode
        super(null, null, new Property[0]);
        // Set
        this.passphraseVault = passphraseVault;
        this.stackTraceVerifier = stackTraceVerifier;

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
        return stackTraceVerifier.verify(Thread.currentThread().getStackTrace()) ? withPassphrase : super.getProperties();
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
        withPassphrase[withPassphrase.length - 1] = new Property(passphraseVault.getPropertyName(), passphraseVault.getPassphrase(), "");
    }

}