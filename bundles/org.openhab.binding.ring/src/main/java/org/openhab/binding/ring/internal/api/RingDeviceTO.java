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
package org.openhab.binding.ring.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingDeviceTO {

    @SerializedName("id")
    public String id = "";

    @SerializedName("kind")
    public String kind = "";

    @SerializedName("description")
    public String description = "";

    @SerializedName("device_id")
    public String deviceId = "";

    @SerializedName("time_zone")
    public String timeZone = "";

    @SerializedName("firmware_version")
    public String firmwareVersion = "";

    public HealthTO health = new HealthTO();

    @SerializedName("battery_life")
    public String battery = "";

    public OwnerTO owner = new OwnerTO();
}
