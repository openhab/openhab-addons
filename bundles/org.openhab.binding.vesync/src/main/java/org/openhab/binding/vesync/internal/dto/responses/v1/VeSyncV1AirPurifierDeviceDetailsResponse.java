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
package org.openhab.binding.vesync.internal.dto.responses.v1;

import org.openhab.binding.vesync.internal.dto.responses.VeSyncResponse;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV1AirPurifierDeviceDetailsResponse} is a Java class used as a DTO to hold the Vesync's V1 API's
 * common response
 * data, in regards to a Air Purifier device.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV1AirPurifierDeviceDetailsResponse extends VeSyncResponse {

    @SerializedName("screenStatus")
    public String screenStatus;

    public String getScreenStatus() {
        return screenStatus;
    }

    @SerializedName("airQuality")
    public int airQuality;

    public int getAirQuality() {
        return airQuality;
    }

    @SerializedName("level")
    public int level;

    public int getLevel() {
        return level;
    }

    @SerializedName("mode")
    public String mode;

    public String getMode() {
        return mode;
    }

    @SerializedName("deviceName")
    public String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    @SerializedName("currentFirmVersion")
    public String currentFirmVersion;

    public String getCurrentFirmVersion() {
        return currentFirmVersion;
    }

    @SerializedName("childLock")
    public String childLock;

    public String getChildLock() {
        return childLock;
    }

    @SerializedName("deviceStatus")
    public String deviceStatus;

    public String getDeviceStatus() {
        return deviceStatus;
    }

    @SerializedName("deviceImg")
    public String deviceImgUrl;

    public String getDeviceImgUrl() {
        return deviceImgUrl;
    }

    @SerializedName("connectionStatus")
    public String connectionStatus;

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public boolean isDeviceOnline() {
        return "online".equals(connectionStatus);
    }
}
