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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Sensor JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class Sensor {

    /** User-supplied sensor name */
    @SerializedName("name")
    public @Nullable String name;

    /** Active flag. False for removed sensors. */
    @SerializedName("active")
    public @Nullable Boolean active;

    /** Hardware device ID */
    @SerializedName("deviceId")
    public @Nullable String deviceId;

    /** MAC Address */
    @SerializedName("address")
    public @Nullable String address;

    /** Sensor battery voltage */
    @SerializedName("battery_voltage")
    public @Nullable Float batteryVoltage;

    /** RSSI expressed in dBm */
    @SerializedName("rssi")
    public @Nullable Integer rssi;

    /** Long form of ID <deviceId>.<streamUUID> */
    @SerializedName("id")
    public @Nullable String id;

    public Sensor() {
    }
}
