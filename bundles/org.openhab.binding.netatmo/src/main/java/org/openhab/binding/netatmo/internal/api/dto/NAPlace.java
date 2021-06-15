/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAPlace {
    private @NonNullByDefault({}) String city;
    private @NonNullByDefault({}) String country;
    private @NonNullByDefault({}) String timezone;
    private @Nullable String street;
    private double altitude;
    private double[] location = {};

    public String getCity() {
        return city;
    }

    public @Nullable String getStreet() {
        return street;
    }

    public String getCountry() {
        return country;
    }

    public String getTimezone() {
        return timezone;
    }

    public @Nullable PointType getLocation() {
        if (location.length == 2) {
            return new PointType(new DecimalType(location[1]), new DecimalType(location[0]), new DecimalType(altitude));
        }
        return null;
    }

}
