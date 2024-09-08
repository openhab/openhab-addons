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
package org.openhab.binding.flume.internal.api.dto;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FlumeApiDevice} dto for FetchUsersDevices.
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiDevice {
    public String id = ""; // "id": "6248148189204194987",
    @SerializedName("bridge_id")
    public String bridgeId; // "bridge_id": "6248148189204155555",
    public int type; // Bridge devices have type=1. Sensor devices have type=2
    public String name;
    public String description;
    @SerializedName("added_datetime")
    public String addedDateTime; // "added_datetime": "2017-03-16T14:30:13.284Z",
    @SerializedName("last_seen")
    public Instant lastSeen; // "last_seen": "2017-04-13T01:31:36.000Z",
    @SerializedName("battery_level")
    public String batteryLevel;
}
