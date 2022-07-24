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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Contains basic information about a single device, from within a VeSyncManagedDevicesPage.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncManagedDeviceBase {

    @SerializedName("deviceRegion")
    public String deviceRegion;

    public String getDeviceRegion() {
        return deviceRegion;
    }

    @SerializedName("deviceType")
    public String deviceType;

    public String getDeviceType() {
        return deviceType;
    }

    @SerializedName("deviceName")
    public String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    @SerializedName("deviceImg")
    public String deviceImg;

    public String getDeviceImg() {
        return deviceImg;
    }

    @SerializedName("deviceStatus")
    public String deviceStatus;

    public String getDeviceStatus() {
        return deviceStatus;
    }

    @SerializedName("cid")
    public String cid;

    public String getCid() {
        return cid;
    }

    @SerializedName("connectionStatus")
    public String connectionStatus;

    public String getConnectionStatus() {
        return connectionStatus;
    }

    @SerializedName("connectionType")
    public String connectionType;

    public String getConnectionType() {
        return connectionType;
    }

    @SerializedName("type")
    public String type;

    public String getType() {
        return type;
    }

    @SerializedName("subDeviceNo")
    public String subDeviceNo;

    public String getSubDeviceNo() {
        return subDeviceNo;
    }

    @SerializedName("subDeviceType")
    public String subDeviceType;

    public String getSubDeviceType() {
        return subDeviceType;
    }

    @SerializedName("uuid")
    public String uuid;

    public String getUuid() {
        return uuid;
    }

    @SerializedName("macID")
    public String macId;

    public String getMacId() {
        return macId;
    }

    @SerializedName("currentFirmVersion")
    public String currentFirmVersion;

    public String getCurrentFirmVersion() {
        return currentFirmVersion;
    }

    @SerializedName("configModule")
    public String configModule;

    public String getConfigModule() {
        return configModule;
    }

    @SerializedName("mode")
    public String mode;

    public String getMode() {
        return mode;
    }

    @SerializedName("speed")
    public String speed;

    public String getSpeed() {
        return speed;
    }
}
