/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single devices connected to the Bosch Smart Home Controller.
 *
 * Example from Json:
 *
 * {
 * "@type":"device",
 * "rootDeviceId":"64-da-a0-02-14-9b",
 * "id":"hdm:HomeMaticIP:3014F711A00004953859F31B",
 * "deviceServiceIds":["PowerMeter","PowerSwitch","PowerSwitchProgram","Routing"],
 * "manufacturer":"BOSCH",
 * "roomId":"hz_3",
 * "deviceModel":"PSM",
 * "serial":"3014F711A00004953859F31B",
 * "profile":"GENERIC",
 * "name":"Coffee Machine",
 * "status":"AVAILABLE",
 * "childDeviceIds":[]
 * }
 *
 * @author Stefan Kästle - Initial contribution
 */
public class Device {

    @SerializedName("@type")
    public String type;

    public String rootDeviceId;
    public String id;
    public List<String> deviceServiceIds;
    public String manufacturer;
    public String roomId;
    public String deviceModel;
    public String serial;
    public String profile;
    public String name;
    public String status;
    public List<String> childDeviceIds;

    public static Boolean isValid(Device obj) {
        return obj != null && obj.id != null;
    }

    @Override
    public String toString() {
        return String.format(
                "Type %s; RootDeviceId: %s; Id: %s; Device Service Ids: %s; Manufacturer: %s; Room Id: %s; Device Model: %s; Serial: %s; Profile: %s; Name: %s; Status: %s; Child Device Ids: %s ",
                this.type, this.rootDeviceId, this.id,
                this.deviceServiceIds != null ? String.join(", ", this.deviceServiceIds) : "null", this.manufacturer,
                this.roomId, this.deviceModel, this.serial, this.profile, this.name, this.status,
                this.childDeviceIds != null ? String.join(", ", this.childDeviceIds) : "null");
    }
}
