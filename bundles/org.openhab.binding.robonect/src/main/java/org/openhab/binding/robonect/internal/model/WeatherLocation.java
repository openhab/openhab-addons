/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Location used by the Robonect weather service.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherLocation {
    private @Nullable Integer cityid;
    private @Nullable String zip;
    private @Nullable String country;

    public @Nullable Integer getCityId() {
        return cityid;
    }

    public @Nullable String getZip() {
        return zip;
    }

    public @Nullable String getCountry() {
        return country;
    }
}
