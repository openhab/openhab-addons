/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtIpInfo} class stores IP data.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtIpInfo {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtIpInfo.class);
    private AsuswrtTraffic traffic = new AsuswrtTraffic();
    private String ifName = "";
    private String hwAddress = "";
    private String ipAddress = "";
    private String ipProto = "";
    private String subnet = "";
    private String gateway = "";
    private String dnsServer = "";
    private Boolean connected = false;

    public AsuswrtIpInfo() {
    }

    /**
     * Constructor.
     *
     * @param ifName name of interface
     * @param jsonObject with ipInfo
     */
    public AsuswrtIpInfo(String ifName, JsonObject jsonObject) {
        this.ifName = ifName;
        traffic = new AsuswrtTraffic(ifName);
        setData(jsonObject);
    }

    /*
     * Setters
     */

    public void setData(JsonObject jsonObject) {
        if (ifName.startsWith(INTERFACE_LAN)) {
            logger.trace("(AsuswrtIpInfo) setData for interface {}", INTERFACE_LAN);
            hwAddress = jsonObjectToString(jsonObject, JSON_MEMBER_MAC, hwAddress);
            ipAddress = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_IP, ipAddress);
            subnet = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_NETMASK, subnet);
            gateway = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_GATEWAY, gateway);
            ipProto = jsonObjectToString(jsonObject, JSON_MEMBER_LAN_PROTO, ipProto);
        } else if (ifName.startsWith(INTERFACE_WAN)) {
            logger.trace("(AsuswrtIpInfo) setData for interface {}", INTERFACE_WAN);
            hwAddress = jsonObjectToString(jsonObject, JSON_MEMBER_MAC, hwAddress);
            ipAddress = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_IP, ipAddress);
            subnet = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_NETMASK, subnet);
            gateway = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_GATEWAY, gateway);
            ipProto = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_PROTO, ipProto);
            dnsServer = jsonObjectToString(jsonObject, JSON_MEMBER_WAN_DNS_SERVER, dnsServer);
            connected = (jsonObjectToInt(jsonObject, JSON_MEMBER_WAN_CONNECTED) == 1);
        }
        if (jsonObject.has(JSON_MEMBER_TRAFFIC)) {
            traffic.setData(jsonObject.getAsJsonObject(JSON_MEMBER_TRAFFIC));
        }
    }

    /*
     * Getters
     */

    public AsuswrtTraffic getTraffic() {
        return traffic;
    }

    public String getMAC() {
        return hwAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSubnet() {
        return subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public String getIpProto() {
        return ipProto;
    }

    public String getDNSNServer() {
        return dnsServer;
    }

    public String getName() {
        return ifName;
    }

    public Boolean isConnected() {
        return connected;
    }
}
