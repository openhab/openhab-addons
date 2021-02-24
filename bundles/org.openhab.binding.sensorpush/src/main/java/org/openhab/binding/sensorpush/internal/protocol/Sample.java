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
package org.openhab.binding.sensorpush.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Sensor Sample JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class Sample {

    // TODO Include altitude?
    // public int altitude; // in feet
    /** Temperature in degrees F with 1 digit of precision */
    public @Nullable Float temperature;
    /** Relative humidity percentage with 1 digit of precision */
    public @Nullable Float humidity;
    /** Barometric pressure in inches of mercury (inHg) */
    @SerializedName("barometric_pressure")
    public @Nullable Float barometricPressure;
    /** Dew point in degrees F with 1 digit of precision */
    public @Nullable Float dewpoint;
    /** Vapor pressure deficit in kPa with 2 digits of precision */
    public @Nullable Float vpd;
    /** Timestamp in format 2021-02-25T21:01:37.000Z */
    public @Nullable String observed; // TODO convert to DateTime?

    public Sample() {
    }
}
