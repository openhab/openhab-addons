/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.test;

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

import org.openhab.binding.energenie.EnergenieBindingConstants

import com.google.gson.annotations.SerializedName

/**
 * JSON representation of a gateway used in the communication with the server.
 *
 * @author Mihaela Memova
 *
 */
public class JsonGateway extends JsonDevice {

    public static final int DEFAULT_USER_ID = 35764;
    public static final int DEFAULT_GATEWAY_ID = 4541;
    public static final String DEFAULT_MAC_ADDRESS = "a0bb3e9013c9";
    public static final String DEFAULT_IP_ADDRESS = "195.24.43.238";
    public static final int DEFAULT_PORT = 49154;
    public static final String DEFAULT_LABEL = "New Gateway";
    public static final String DEFAULT_AUTH_CODE = "a21f913b022d";
    public static final String DEFAULT_TYPE = "gateway";
    public static final String DEFAULT_LAST_SEEN = getFormattedCurrentDayTime();
    public static final int DEFAULT_FIRMWARE_VERSION = 13;

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

    public JsonGateway() {
        super(DEFAULT_TYPE, DEFAULT_GATEWAY_ID, DEFAULT_LABEL);
        this.userID = DEFAULT_USER_ID;
        this.macAddress = DEFAULT_MAC_ADDRESS;
        this.ipAddress = DEFAULT_IP_ADDRESS;
        this.port = DEFAULT_PORT;
        this.authCode = DEFAULT_AUTH_CODE;
        this.firmwareVersionID = DEFAULT_FIRMWARE_VERSION;
        this.lastSeenAt = DEFAULT_LAST_SEEN;
    }

    public JsonGateway(int id) {
        super(DEFAULT_TYPE, id, DEFAULT_LABEL);
        this.userID = DEFAULT_USER_ID;
        this.macAddress = DEFAULT_MAC_ADDRESS;
        this.ipAddress = DEFAULT_IP_ADDRESS;
        this.port = DEFAULT_PORT;
        this.authCode = DEFAULT_AUTH_CODE;
        this.firmwareVersionID = DEFAULT_FIRMWARE_VERSION;
        this.lastSeenAt = DEFAULT_LAST_SEEN;
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

    public static String getFormattedCurrentDayTime() {
        LocalDateTime curentLocalDateTime = LocalDateTime.now();
        Date currentDate = Date.from(curentLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return new SimpleDateFormat(EnergenieBindingConstants.DATE_TIME_PATTERN).format(currentDate);
    }
}
