package com.davidcubesvk.securedNetworkCore.authenticator;

/**
 * Property class used to parse a JSON property array into an array of instances of this class.
 */
public class Property {

    //Name, value and signature
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
