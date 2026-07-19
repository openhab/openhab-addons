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
package org.openhab.binding.autoblind.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Status data for a single shade peripheral as returned by the hub API.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
public class PeripheralStatus {

    @SerializedName("PeripheralUID")
    public int peripheralUid;

    @SerializedName("ModuleType")
    public int moduleType;

    @SerializedName("ModuleDetail")
    public int moduleDetail;

    @SerializedName("RoomID")
    public int roomId;

    @SerializedName("GroupID")
    public int groupId;

    @SerializedName("BatteryVoltage")
    public int batteryVoltage;

    @SerializedName("BottomRailPosition")
    public int bottomRailPosition;

    @SerializedName("TargetBottomRailPosition")
    public int targetBottomRailPosition;

    @SerializedName("MiddleRailPosition")
    public int middleRailPosition;

    @SerializedName("TargetMiddleRailPosition")
    public int targetMiddleRailPosition;

    @SerializedName("RssiMean")
    public int rssiMean;

    @SerializedName("RfFirmwareVersion")
    public String rfFirmwareVersion = "";

    @SerializedName("FirmwareVersion")
    public String firmwareVersion = "";

    @SerializedName("Timestamp")
    public long timestamp;
}
