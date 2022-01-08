/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Sample Response JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class SamplesResponse extends Response {

    @SerializedName("truncated")
    public @Nullable Boolean truncated;

    @SerializedName("total_sensors")
    public @Nullable Integer totalSensors;

    @SerializedName("last_time")
    public @Nullable String lastTime;

    /** Status. "OK" = good */
    public @Nullable String status;

    @SerializedName("total_samples")
    public @Nullable Integer totalSamples;

    @SerializedName("sensors")
    public @Nullable Map<String, Sample[]> sensors;

    public SamplesResponse() {
    }
}
