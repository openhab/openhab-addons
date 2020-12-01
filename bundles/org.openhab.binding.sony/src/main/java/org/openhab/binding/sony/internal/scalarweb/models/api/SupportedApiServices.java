/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the service supported by the API and is used to request information
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApiServices {
    /** The sevices */
    private final String[] services;

    /**
     * Constructs the class from teh specified services
     * 
     * @param services a non-empty list of services
     */
    public SupportedApiServices(final String... services) {
        if (services.length == 0) {
            throw new IllegalArgumentException("services must have atlease one");
        }
        this.services = services;
    }

    /**
     * Returns the services
     * 
     * @return a non-null, non empty array of services
     */
    public String[] getServices() {
        return services;
    }

    @Override
    public String toString() {
        return "SupportedApiServices [services=" + Arrays.toString(services) + "]";
    }
}
