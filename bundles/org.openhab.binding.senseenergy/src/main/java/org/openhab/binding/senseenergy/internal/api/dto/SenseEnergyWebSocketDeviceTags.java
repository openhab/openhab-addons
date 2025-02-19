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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyWebSocketDeviceTags } is the dto for tag info inside the SenseEnergyApiDevice dto class
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyWebSocketDeviceTags {
    @SerializedName("DUID")
    public String deviceID;
    @SerializedName("SSIEnabled")
    public boolean ssiEnabled;
}

/* @formatter:off
    "tags": {
        "DefaultUserDeviceType": "ElectricVehicle",
        "DeviceListAllowed": "true",
        "Stage": "Tracking",
        "UserAdded": "false",
        "UserDeviceType": "ElectricVehicle",
        "UserEditable": "false",
        "UserMergeable": "false"
              "Alertable": "false",
      "AlwaysOn": "false",
      "DateCreated": "2024-05-09T04:16:08.000Z",
      "DateFirstUsage": "2024-04-02",
      "DefaultUserDeviceType": "AC",
      "DeployToMonitor": "true",
      "DeviceListAllowed": "false",
      "ModelCreatedVersion": "21",
      "ModelUpdatedVersion": "28",
      "name_useredit": "true",
      "OriginalName": "AC 2",
      "Pending": "false",
      "Revoked": "false",
      "TimelineAllowed": "false",
      "TimelineDefault": "false",
      "Type": "AC",
      "UserDeletable": "true",
      "UserDeleted": "true",
      "UserDeviceType": "OtherDevice",
      "UserDeviceTypeDisplayString": "Other Device",
      "UserEditable": "false",
      "UserEditableMeta": "true",
      "UserMergeable": "true",
      "UserShowInDeviceList": "true"
      },
      {
        "id": "zDcE5VOr",
        "name": "Sense Whole House Proxy Fan",
        "icon": "plug",
        "tags": {
            "Alertable": "false",
            "ControlCapabilities": [
                "OnOff",
                "StandbyThreshold"
            ],
            "DateCreated": "2024-09-22T03:13:45.000Z",
            "DefaultUserDeviceType": "SmartPlug",
            "DeviceListAllowed": "false",
            "DUID": "53:75:31:70:55:53",
            "IntegratedDeviceType": "IntegratedSmartPlug",
            "IntegrationType": "TPLink",
            "name_useredit": "false",
            "OriginalName": "Sense Whole House Proxy Fan",
            "Revoked": "false",
            "SmartPlugModel": "TP-Link Kasa HS110",
            "SSIEnabled": "true",
            "SSIModel": "SelfReporting",
            "TimelineAllowed": "false",
            "TimelineDefault": "false",
            "UserDeletable": "false",
            "UserDeleted": "true",
            "UserDeviceType": "SmartPlug",
            "UserDeviceTypeDisplayString": "Smart Plug",
            "UserEditable": "false",
            "UserEditableMeta": "true",
            "UserMergeable": "false",
            "UserShowInDeviceList": "true",
            "UserVisibleDeviceId": "53:75:31:70:55:53"
        }
    },
 * @formatter:on
 */
