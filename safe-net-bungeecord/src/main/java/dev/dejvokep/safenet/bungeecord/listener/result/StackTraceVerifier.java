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

import dev.dejvokep.safenet.bungeecord.SafeNetBungeeCord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A verifier for method caller origins (stack trace).
 * <p>
 * This class should only be used to verify callers of profile property getters, in order to avoid leaking secure
 * property to 3rd parties.
 */
public class StackTraceVerifier {

    /**
     * Underlying server implementation used.
     */
    private enum Server {
        GENERIC("generic"), FLAMECORD("FlameCord");

        private String name;

        /**
         * Constructs a server definition.
         *
         * @param name a human-readable server name representation
         */
        Server(String name) {
            this.name = name;
        }
    }

    /**
     * The first call stack element (after internal SafeNET methods). Must be superseded by {@link #LAST_ELEMENT} or any
     * of {@link #MIDDLEWARES}, if applicable.
     */
    private static final StackTraceElement FIRST_ELEMENT = new StackTraceElement("net.md_5.bungee.ServerConnector", "connected", null, -1);

    /**
     * The last call stack element. Must be preceded by {@link #FIRST_ELEMENT} or any of {@link #MIDDLEWARES}, if
     * applicable.
     */
    private static final StackTraceElement LAST_ELEMENT = new StackTraceElement("net.md_5.bungee.netty.HandlerBoss", "channelActive", null, -1);

    /**
     * All permitted middlewares (call stack elements between {@link #FIRST_ELEMENT} and {@link #LAST_ELEMENT}).
     */
    private static final Map<Server, StackTraceElement> MIDDLEWARES = Collections.unmodifiableMap(new HashMap<Server, StackTraceElement>() {{
        put(Server.GENERIC, null);
        put(Server.FLAMECORD, new StackTraceElement("dev._2lstudios.flamecord.forge.ModernForgeServerConnector", "connected", null, -1));
    }});

    // Server in use
    private Server server = Server.GENERIC;

    /**
     * Initializes the verifier.
     *
     * @param plugin the plugin
     */
    public StackTraceVerifier(SafeNetBungeeCord plugin) {
        for (Map.Entry<Server, StackTraceElement> entry : MIDDLEWARES.entrySet()) {
            if (entry.getValue() == null)
                return;

            try {
                Class.forName(entry.getValue().getClassName());
                server = entry.getKey();
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        plugin.getLogger().info(String.format("Detected %s server software. Accepting %s when validating secure profile properties request source.", server.name, server == Server.GENERIC ? "no middleware" : "middleware"));
    }

    /**
     * Verifies if the stack trace represents an internal server call. The return value also indicates if the passphrase
     * must be included in the outputted property array.
     * <p>
     * The array is checked with an offset of 2, as the first element should always be the method that was called to
     * <i>obtain</i> this trace ({@link Thread#getStackTrace()}), and the second the profile property getter method
     * ({@link SafeNetLoginResult#getProperties()}).
     *
     * @param stackTrace the stack trace to verify
     * @return if the call was made by internal server components responsible for socket channel handling, and
     * therefore, it is safe to include the passphrase in the returned property array
     */
    public boolean verify(StackTraceElement[] stackTrace) {
        if (stackTrace.length < 4)
            return false;
        if (!compareElements(stackTrace[2], FIRST_ELEMENT))
            return false;
        if (compareElements(stackTrace[3], LAST_ELEMENT))
            return true;
        if (stackTrace.length < 5)
            return false;

        StackTraceElement middleware = MIDDLEWARES.get(server);
        if (middleware == null)
            return false;

        return compareElements(stackTrace[3], middleware) && compareElements(stackTrace[4], LAST_ELEMENT);
    }

    /**
     * Compares class and method names of the two given trace elements.
     *
     * @param real     the real element
     * @param expected the expected element
     * @return if the two elements match
     */
    private boolean compareElements(StackTraceElement real, StackTraceElement expected) {
        return real.getClassName().equals(expected.getClassName()) && real.getMethodName().equals(expected.getMethodName());
    }

}