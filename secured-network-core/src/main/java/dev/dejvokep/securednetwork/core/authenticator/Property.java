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
package dev.dejvokep.securednetwork.core.authenticator;

/**
 * Property class used to parse a JSON property array into an array of instances of this class.
 */
public class Property {

    // Name, value and signature
    private String name, value, signature;

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
     * Returns the signature passed with the property.
     *
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }
}
