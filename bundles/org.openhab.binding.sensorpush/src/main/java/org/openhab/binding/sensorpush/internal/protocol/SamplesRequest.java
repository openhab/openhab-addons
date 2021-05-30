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

/**
 * Samples Request JSON object
 *
 * @author Bob Adair - Initial contribution
 *
 *         Note: Expected time format is 2019-04-07T00:00:00-0400
 */
@NonNullByDefault
public class SamplesRequest {

    public static final String[] ALL_MEASUREMENTS = { "temperature", "humidity", "vpd", "barometric_pressure",
            "dewpoint", "altitude" };

    /** Return samples for only active sensors (default=true) */
    public @Nullable Boolean active;
    /** Number of samples to return */
    public @Nullable Integer limit;
    /** Measurements to return in samples. Valid values: temperature|humidity|vpd|barometric_pressure|dewpoint */
    public @NonNullByDefault({}) String[] measures;
    /** Sensor ids to return samples for */
    public @NonNullByDefault({}) String[] sensors;
    /** Start time. Leave blank for most recent */
    public @Nullable String startTime;
    /** Stop time. Leave blank for most recent */
    public @Nullable String stopTime;

    public SamplesRequest() {
    }
}
