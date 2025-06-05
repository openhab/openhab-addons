/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.ApiConstants;

import com.google.gson.annotations.SerializedName;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingDeviceTO {

    @SerializedName(ApiConstants.DEVICE_ID)
    public String id = "";

    @SerializedName(ApiConstants.DEVICE_KIND)
    public String kind = "";

    @SerializedName(ApiConstants.DEVICE_DESCRIPTION)
    public String description = "";

    @SerializedName(ApiConstants.DEVICE_DEVICE_ID)
    public String deviceId = "";

    @SerializedName(ApiConstants.DEVICE_TIME_ZONE)
    public String timeZone = "";

    @SerializedName(ApiConstants.DEVICE_FIRMWARE_VERSION)
    public String firmwareVersion = "";

    public @NonNullByDefault({}) Health health;

    @SerializedName(ApiConstants.DEVICE_BATTERY)
    public String battery = "";

    public class Health {
        @SerializedName("battery_percentage")
        public int batteryPercentage;
    }

    private RingDeviceTO() {
    }
}
