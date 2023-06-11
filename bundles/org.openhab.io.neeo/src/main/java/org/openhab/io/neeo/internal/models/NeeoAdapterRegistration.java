/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing an adapter registration (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
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
        NeeoUtil.requireNotEmpty(name, "name cannot be empty");
        NeeoUtil.requireNotEmpty(baseUrl, "baseUrl cannot be empty");

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
