/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api;

import org.openhab.binding.energenie.internal.api.constants.EnergenieDeviceTypes;

import com.google.gson.annotations.SerializedName;

/**
 * JSON representation of a gateway used in the communication with the server.
 *
 * @author Mihaela Memova
 *
 */
public class JsonGateway extends JsonDevice {
    public static final String DEFAULT_TYPE = "gateway";

    @SerializedName("user_id")
    int userID;

    @SerializedName("mac_address")
    String macAddress;

    @SerializedName("ip_address")
    String ipAddress;

    @SerializedName("port")
    int port;

    @SerializedName("auth_code")
    String authCode;

    @SerializedName("firmware_version_id")
    int firmwareVersionID;

    @SerializedName("last_seen_at")
    String lastSeenAt;

    public JsonGateway(int userID, int id, String label, String authCode, String macAddress, String ipAddress, int port,
            int firmwareVersionID, String lastSeenAt) {
        super(EnergenieDeviceTypes.GATEWAY, id, label);
        this.authCode = authCode;
        this.userID = userID;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.port = port;
        this.firmwareVersionID = firmwareVersionID;
        this.lastSeenAt = lastSeenAt;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMac_address(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public int getFirmwareVersionID() {
        return firmwareVersionID;
    }

    public void setFirmwareVersionID(int firmwareVersionID) {
        this.firmwareVersionID = firmwareVersionID;
    }

    public String getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(String lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
