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
package dev.dejvokep.safenet.spigot.authentication;

import org.jetbrains.annotations.NotNull;

/**
 * A property record used to parse a JSON property array.
 */
public class RawProperty {

    // Name, value and signature
    private String name, value, signature;

    /**
     * Plain constructor, to be used by GSON.
     */
    public RawProperty() {
    }

    /**
     * Constructs a property with the given data.
     * <p>
     * <b>It is unknown if the server also accepts properties with <code>null</code> data, such usage is therefore
     * deprecated.</b>
     *
     * @param name      the name
     * @param value     the value
     * @param signature the signature
     */
    public RawProperty(@NotNull String name, @NotNull String value, @NotNull String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    /**
     * Returns the name of the property.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the signature of the property.
     *
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }
}
