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
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.INTERFACE_CLIENT;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtClientInfo} class stores client data.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtClientInfo {
    private AsuswrtTraffic traffic = new AsuswrtTraffic();
    private Integer defaultType = 0;
    private String dpiDevice = "";
    private String dpiType = "";
    private String from = "";
    private String group = "";
    private String internetMode = "";
    private Boolean internetState = false;
    private String ip = "";
    private String ipMethod = "";
    private Boolean isGateway = false;
    private Boolean isGN = false;
    private Boolean isITunes = false;
    private Boolean isLogin = false;
    private Boolean isOnline = false;
    private Boolean isPrinter = false;
    private Boolean isWebServer = false;
    private Integer isWL = 0;
    private String keeparp = "";
    private String mac = "";
    private Boolean macRepeat = false;
    private String name = "";
    private String nickName = "";
    private Integer opMode = 0;
    private String qosLevel = "";
    private Integer rog = 0;
    private Integer rssi = 0;
    private String ssid = "";
    private String vendor = "";
    private String wlConnectTime = "";
    private Integer wtfast = 0;

    public AsuswrtClientInfo() {
    }

    public AsuswrtClientInfo(JsonObject jsonObject) {
        traffic = new AsuswrtTraffic(INTERFACE_CLIENT);
        setData(jsonObject);
    }

    public void setData(JsonObject jsonObject) {
        traffic.setData(jsonObject);
        defaultType = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_DEFTYPE, defaultType);
        dpiDevice = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_DPIDEVICE, dpiDevice);
        dpiType = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_DPITYPE, dpiType);
        from = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IPFROM, from);
        group = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_GROUP, group);
        internetMode = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_INETMODE, internetMode);
        internetState = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_INETSTATE, internetState);
        ip = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IP, ip);
        ipMethod = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IPMETHOD, ipMethod);
        isGateway = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_IPGATEWAY, isGateway);
        isGN = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_GN, isGN);
        isITunes = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_ITUNES, isITunes);
        isLogin = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_LOGIN, isLogin);
        isOnline = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_ONLINE, isOnline);
        isPrinter = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_PRINTER, isPrinter);
        isWebServer = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_WEBSRV, isWebServer);
        isWL = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_WIFI, isWL);
        keeparp = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_KEEPARP, keeparp);
        mac = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_MAC, mac);
        macRepeat = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_MACREPEAT, macRepeat);
        name = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_NAME, name);
        nickName = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_NICK, nickName);
        opMode = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_MODE, opMode);
        qosLevel = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_QOSLVL, qosLevel);
        rog = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_ROG, rog);
        rssi = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RSSI, rssi);
        ssid = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_SSID, ssid);
        vendor = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_VENDOR, vendor);
        wlConnectTime = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_CONNECTTIME, wlConnectTime);
        wtfast = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_WTFAST, wtfast);
    }

    /*
     * Getters
     */

    public AsuswrtTraffic getTraffic() {
        return traffic;
    }

    public Integer getDefaultType() {
        return defaultType;
    }

    public String getDpiDevice() {
        return dpiDevice;
    }

    public String getDpiType() {
        return dpiType;
    }

    public String getIpFrom() {
        return from;
    }

    public String getGroup() {
        return group;
    }

    public String getInternetMode() {
        return internetMode;
    }

    public Boolean getInternetState() {
        return internetState;
    }

    public String getIP() {
        return ip;
    }

    public String getIpMethod() {
        return ipMethod;
    }

    public Boolean isGateway() {
        return isGateway;
    }

    public Boolean isGN() {
        return isGN;
    }

    public Boolean isITunes() {
        return isITunes;
    }

    public Boolean isLogin() {
        return isLogin;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public Boolean isPrinter() {
        return isPrinter;
    }

    public Boolean isWebServer() {
        return isWebServer;
    }

    public Integer isWL() {
        return isWL;
    }

    public Boolean isWiFiConnected() {
        return isWL > 0;
    }

    public String getKeepArp() {
        return keeparp;
    }

    public String getMac() {
        return mac;
    }

    public Boolean getMacRepeat() {
        return macRepeat;
    }

    public String getName() {
        return name;
    }

    public String getNickName() {
        return nickName;
    }

    public Integer getOpMode() {
        return opMode;
    }

    public String getQosLevel() {
        return qosLevel;
    }

    public Integer getROG() {
        return rog;
    }

    public Integer getRSSI() {
        return rssi;
    }

    public String getSSID() {
        return ssid;
    }

    public String getVendor() {
        return vendor;
    }

    public String getWlanConnectTime() {
        return wlConnectTime;
    }

    public Integer getWtFast() {
        return wtfast;
    }
}
