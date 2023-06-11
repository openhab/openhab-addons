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
package org.openhab.binding.ventaair.internal.message.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Header which is part of a message to/from a device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class Header {

    @SerializedName(value = "MacAdress")
    private String macAdress;

    @SerializedName(value = "IpAdress")
    private String ipAdress;

    @SerializedName(value = "DeviceType")
    private int deviceType;

    @SerializedName(value = "Hash")
    private String hash;

    @SerializedName(value = "DeviceName")
    private String deviceName;

    public Header(String mac, int devType, String hash, String devName) {
        this.macAdress = mac;
        this.deviceType = devType;
        this.hash = hash;
        this.deviceName = devName;
    }

    public String getMacAdress() {
        return macAdress;
    }

    public void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
