/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Place} reports location information of a Netatmo system.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Place implements LocationEx {
    private @Nullable String city;
    private @Nullable String country;
    private @Nullable String timezone;
    private double altitude;
    private double[] location = {};

    public Optional<String> getCity() {
        return Optional.ofNullable(city);
    }

    @Override
    public Optional<String> getCountry() {
        return Optional.ofNullable(country);
    }

    @Override
    public @Nullable String getTimezone() {
        return timezone;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public double[] getCoordinates() {
        return location;
    }
}
