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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the parental information
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ParentalInfo {
    /** The rating of the content */
    private @Nullable String rating;

    /** The rating system used */
    private @Nullable String system;

    /** The rating country */
    private @Nullable String country;

    /**
     * Constructor used for deserialization only
     */
    public ParentalInfo() {
    }

    /**
     * Gets the content rating
     * 
     * @return the content rating
     */
    public @Nullable String getRating() {
        return rating;
    }

    /**
     * Gets the rating system
     * 
     * @return the rating system
     */
    public @Nullable String getSystem() {
        return system;
    }

    /**
     * Gets the rating country
     * 
     * @return the rating country
     */
    public @Nullable String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "ParentalInfo [rating=" + rating + ", system=" + system + ", country=" + country + "]";
    }
}
