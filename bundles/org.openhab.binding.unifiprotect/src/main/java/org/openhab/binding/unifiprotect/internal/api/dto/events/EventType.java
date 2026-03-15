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
package org.openhab.binding.unifiprotect.internal.api.dto.events;

import org.openhab.binding.unifiprotect.internal.api.dto.ApiValueEnum;

import com.google.gson.annotations.SerializedName;

/**
 * Event type discriminator values used by the API.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum EventType implements ApiValueEnum {
    @SerializedName("ring")
    RING("ring"),
    @SerializedName("sensorExtremeValues")
    SENSOR_EXTREME_VALUES("sensorExtremeValues"),
    @SerializedName("sensorWaterLeak")
    SENSOR_WATER_LEAK("sensorWaterLeak"),
    @SerializedName("sensorTamper")
    SENSOR_TAMPER("sensorTamper"),
    @SerializedName("sensorBatteryLow")
    SENSOR_BATTERY_LOW("sensorBatteryLow"),
    @SerializedName("sensorAlarm")
    SENSOR_ALARM("sensorAlarm"),
    @SerializedName("sensorOpened")
    SENSOR_OPENED("sensorOpened"),
    @SerializedName("sensorClosed")
    SENSOR_CLOSED("sensorClosed"),
    @SerializedName("sensorMotion")
    SENSOR_MOTION("sensorMotion"),
    @SerializedName("lightMotion")
    LIGHT_MOTION("lightMotion"),
    @SerializedName("motion")
    CAMERA_MOTION("motion"),
    @SerializedName("smartAudioDetect")
    SMART_AUDIO_DETECT("smartAudioDetect"),
    @SerializedName("smartDetectZone")
    SMART_DETECT_ZONE("smartDetectZone"),
    @SerializedName("smartDetectLine")
    SMART_DETECT_LINE("smartDetectLine"),
    @SerializedName("smartDetectLoiterZone")
    SMART_DETECT_LOITER_ZONE("smartDetectLoiterZone");

    private final String apiValue;

    EventType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
