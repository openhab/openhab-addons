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
package org.openhab.binding.asuswrt.internal.structures;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtIPInfo} class stores ip data
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtIpInfo {
    private String ifName = "";
    private String hwAddress = "";
    private String ipAddress = "";
    private String ipProto = "";
    private String subnet = "";
    private String gateway = "";
    private String dnsServer = "";
    private Boolean connected = false;

    /**
     * INIT CLASS
     */
    public AsuswrtIpInfo() {
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param interfaceName name of interface
     * @param jsonObject with ipInfo
     */
    public AsuswrtIpInfo(String ifName, JsonObject jsonObject) {
        this.ifName = ifName;
        setData(jsonObject);
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * Set Data from jsonObject
     * 
     * @param jsonObject jsonObject data is stored
     * @param channelGroup channelGroup data belongs to
     */
    public void setData(JsonObject jsonObject) {
        if (ifName.startsWith(INTERFACE_LAN)) {
            this.hwAddress = jsonObjectToString(jsonObject, JSON_MEMBER_MAC, this.hwAddress);
            this.ipAddress = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_IP, this.ipAddress);
            this.subnet = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_NETMASK, this.subnet);
            this.gateway = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_GATEWAY, this.gateway);
            this.ipProto = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_PROTO, this.ipProto);
        } else if (ifName.startsWith(INTERFACE_WAN)) {
            this.hwAddress = jsonObjectToString(jsonObject, JSON_MEMBER_MAC, this.hwAddress);
            this.ipAddress = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_IP, this.ipAddress);
            this.subnet = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_NETMASK, this.subnet);
            this.gateway = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_GATEWAY, this.gateway);
            this.ipProto = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_PROTO, this.ipProto);
            this.dnsServer = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_DNS_SERVER, this.dnsServer);
            this.connected = (jsonObjectToInt(jsonObject, JSON_MEMBER_WAN_CONNECTED).equals(1));
        } else if (ifName.startsWith(INTERFACE_WLAN)) {
            // ToDo
        }
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getMAC() {
        return this.hwAddress;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getSubnet() {
        return this.subnet;
    }

    public String getGateway() {
        return this.gateway;
    }

    public String getIpProto() {
        return this.ipProto;
    }

    public String getDNSNServer() {
        return this.dnsServer;
    }

    public String getName() {
        return this.ifName;
    }

    public Boolean isConnected() {
        return this.connected;
    }
}
