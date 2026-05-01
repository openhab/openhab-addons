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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.types;

import com.google.gson.annotations.SerializedName;

/**
 * Event types from UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum EventType {
    @SerializedName("motion")
    MOTION,

    @SerializedName("ring")
    RING,

    @SerializedName("smartDetectZone")
    SMART_DETECT,

    @SerializedName("smartDetectLine")
    SMART_DETECT_LINE,

    @SerializedName("smartAudioDetect")
    SMART_AUDIO_DETECT,

    @SerializedName("nfcCardScanned")
    NFC_CARD_SCANNED,

    @SerializedName("fingerprintIdentified")
    FINGERPRINT_IDENTIFIED,

    @SerializedName("sensorMotion")
    MOTION_SENSOR,

    @SerializedName("sensorOpened")
    SENSOR_OPENED,

    @SerializedName("sensorClosed")
    SENSOR_CLOSED,

    @SerializedName("sensorAlarm")
    SENSOR_ALARM,

    @SerializedName("sensorExtremeValues")
    SENSOR_EXTREME_VALUE,

    @SerializedName("sensorWaterLeak")
    SENSOR_WATER_LEAK,

    @SerializedName("sensorBatteryLow")
    SENSOR_BATTERY_LOW,

    @SerializedName("lightMotion")
    MOTION_LIGHT,

    @SerializedName("doorAccess")
    DOOR_ACCESS,

    @SerializedName("disconnect")
    DISCONNECT,

    @SerializedName("factoryReset")
    FACTORY_RESET,

    @SerializedName("provision")
    PROVISION,

    @SerializedName("update")
    UPDATE,

    @SerializedName("cameraPowerCycling")
    CAMERA_POWER_CYCLE,

    @SerializedName("resolutionLowered")
    RESOLUTION_LOWERED,

    @SerializedName("poorConnection")
    POOR_CONNECTION,

    @SerializedName("streamRecovery")
    STREAM_RECOVERY,

    @SerializedName("recordingDeleted")
    RECORDING_DELETED,

    @SerializedName("nonScheduledRecording")
    NO_SCHEDULE,

    @SerializedName("recordingModeChanged")
    RECORDING_MODE_CHANGED,

    @SerializedName("hotplug")
    HOTPLUG,

    @SerializedName("cameraConnected")
    CAMERA_CONNECTED,

    @SerializedName("cameraRebooted")
    CAMERA_REBOOTED,

    @SerializedName("cameraDisconnected")
    CAMERA_DISCONNECTED,

    @SerializedName("deviceAdopted")
    DEVICE_ADOPTED,

    @SerializedName("deviceUnadopted")
    DEVICE_UNADOPTED,

    @SerializedName("deviceConnected")
    DEVICE_CONNECTED,

    @SerializedName("deviceDisconnected")
    DEVICE_DISCONNECTED,

    @SerializedName("deviceRebooted")
    DEVICE_REBOOTED,

    @SerializedName("offline")
    OFFLINE,

    @SerializedName("reboot")
    REBOOT,

    @SerializedName("fwUpdate")
    FIRMWARE_UPDATE,

    @SerializedName("unknown")
    UNKNOWN
}
