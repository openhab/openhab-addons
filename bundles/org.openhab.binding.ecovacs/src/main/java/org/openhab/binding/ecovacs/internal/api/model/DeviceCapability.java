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
package org.openhab.binding.ecovacs.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public enum DeviceCapability {
    @SerializedName("mopping_system")
    MOPPING_SYSTEM,
    @SerializedName("main_brush")
    MAIN_BRUSH,
    @SerializedName("voice_reporting")
    VOICE_REPORTING,
    @SerializedName("spot_area_cleaning")
    SPOT_AREA_CLEANING,
    @SerializedName("custom_area_cleaning")
    CUSTOM_AREA_CLEANING,
    @SerializedName("single_room_cleaning")
    SINGLE_ROOM_CLEANING,
    @SerializedName("clean_speed_control")
    CLEAN_SPEED_CONTROL,
    @SerializedName("mapping")
    MAPPING,
    @SerializedName("auto_empty_station")
    AUTO_EMPTY_STATION,
    @SerializedName("read_network_info")
    READ_NETWORK_INFO,
    @SerializedName("true_detect_3d")
    TRUE_DETECT_3D,
    @SerializedName("unit_care_lifespan")
    UNIT_CARE_LIFESPAN,
    // implicit capabilities added in code
    EDGE_CLEANING,
    SPOT_CLEANING,
    EXTENDED_CLEAN_SPEED_CONTROL,
    EXTENDED_CLEAN_LOG_RECORD,
    DEFAULT_CLEAN_COUNT_SETTING
}
