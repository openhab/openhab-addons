/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

/**
 * The model representing an adapter registration (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoAdapterRegistration {

    /** The name. */
    private final String name;

    /** The callback url */
    private final String baseUrl;

    /**
     * Creates the adapter registration from the name and callback url
     *
     * @param name the name
     * @param baseUrl the base url
     */
    public NeeoAdapterRegistration(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the callback url.
     *
     * @return the callback url
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        return "NeeoAdapterRegistration [name=" + name + ", baseUrl=" + baseUrl + "]";
    }
}
