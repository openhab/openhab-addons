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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiDeviceTags } is the dto for tag info inside the SenseEnergyApiDevice dto class
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiDeviceTags {
    public enum Stage {
        Tracking,
        Inventory
    }

    @SerializedName("Stage")
    public Stage stage;
    @SerializedName("UserDeleted")
    public boolean userDeleted;
    @SerializedName("AlwaysOn")
    public boolean alwaysOn;
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
 * @formatter:on
 */
