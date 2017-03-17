/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.openhab.binding.mihome.MiHomeBindingConstants;

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

    int user_id;
    String mac_address;
    String ip_address;
    int port;
    String auth_code;
    int firmware_version_id;
    String last_seen_at;

    public JsonGateway() {
        super(DEFAULT_TYPE, DEFAULT_GATEWAY_ID, DEFAULT_LABEL);
        this.user_id = DEFAULT_USER_ID;
        this.mac_address = DEFAULT_MAC_ADDRESS;
        this.ip_address = DEFAULT_IP_ADDRESS;
        this.port = DEFAULT_PORT;
        this.auth_code = DEFAULT_AUTH_CODE;
        this.firmware_version_id = DEFAULT_FIRMWARE_VERSION;
        this.last_seen_at = DEFAULT_LAST_SEEN;
    }

    public JsonGateway(int id) {
        super(DEFAULT_TYPE, id, DEFAULT_LABEL);
        this.user_id = DEFAULT_USER_ID;
        this.mac_address = DEFAULT_MAC_ADDRESS;
        this.ip_address = DEFAULT_IP_ADDRESS;
        this.port = DEFAULT_PORT;
        this.auth_code = DEFAULT_AUTH_CODE;
        this.firmware_version_id = DEFAULT_FIRMWARE_VERSION;
        this.last_seen_at = DEFAULT_LAST_SEEN;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAuth_code() {
        return auth_code;
    }

    public void setAuth_code(String auth_code) {
        this.auth_code = auth_code;
    }

    public int getFirmware_version_id() {
        return firmware_version_id;
    }

    public void setFirmware_version_id(int firmware_version_id) {
        this.firmware_version_id = firmware_version_id;
    }

    public String getLast_seen_at() {
        return last_seen_at;
    }

    public void setLast_seen_at(String last_seen_at) {
        this.last_seen_at = last_seen_at;
    }

    public static String getFormattedCurrentDayTime() {
        LocalDateTime curentLocalDateTime = LocalDateTime.now();
        Date currentDate = Date.from(curentLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return new SimpleDateFormat(MiHomeBindingConstants.LAST_SEEN_PROPERTY_PATTERN).format(currentDate);
    }
}
