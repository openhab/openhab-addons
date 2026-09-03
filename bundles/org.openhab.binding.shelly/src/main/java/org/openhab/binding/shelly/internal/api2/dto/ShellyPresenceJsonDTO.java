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
package org.openhab.binding.shelly.internal.api2.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyPresenceJsonDTO} includes constants and structures used for the Shelly Presence Gen4 mmWave radar
 * sensor's JSON mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyPresenceJsonDTO {
    public static final String SHELLYRPC_METHOD_PRESENCE_SETSENSOR = "Presence.SetSensor";
    public static final String SHELLY2_PRESENCE_ZONE_PREFIX = "presencezone:";
    public static final int SHELLY2_PRESENCE_DEFAULT_ZONE_ID = 200;
    public static final String SHELLY2_EVENT_PRESENCE = "presence";
    public static final String SHELLY2_EVENT_COUNTER = "counter";

    public static class Shelly2DevConfigPresence {
        public @Nullable Boolean enable;
        @SerializedName("main_zone")
        public @Nullable String mainZone;
    }

    public static class Shelly2StatusPresence {
        public @Nullable Integer id;
        public @Nullable Boolean value;
        @SerializedName("num_objects")
        public @Nullable Integer numObjects;
    }
}
