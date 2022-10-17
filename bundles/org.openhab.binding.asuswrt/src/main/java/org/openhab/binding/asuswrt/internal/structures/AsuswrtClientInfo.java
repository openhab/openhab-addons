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
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtClientInfo} class stores client data
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtClientInfo {
    private Integer curRx = 0;
    private Integer curTx = 0;
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
    private Integer ROG = 0;
    private Integer rssi = 0;
    private String ssid = "";
    private Integer totalRx = 0;
    private Integer totalTx = 0;
    private String vendor = "";
    private String wlConnectTime = "";
    private Integer wtfast = 0;

    /*
     * INIT CLASS
     */
    public AsuswrtClientInfo() {
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param jsonObject with clientinfo
     */
    public AsuswrtClientInfo(JsonObject jsonObject) {
        setData(jsonObject);
    }

    /**
     * SET DATA
     * from jsonData
     * 
     * @param jsonObject
     */
    public void setData(JsonObject jsonObject) {
        this.curRx = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXCUR, this.curRx);
        this.curTx = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXCUR, this.curTx);
        this.defaultType = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_DEFTYPE, this.defaultType);
        this.dpiDevice = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_DPIDEVICE, this.dpiDevice);
        this.dpiType = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_DPITYPE, this.dpiType);
        this.from = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IPFROM, this.from);
        this.group = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_GROUP, this.group);
        this.internetMode = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_INETMODE, this.internetMode);
        this.internetState = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_INETSTATE, this.internetState);
        this.ip = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IP, this.ip);
        this.ipMethod = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_IPMETHOD, this.ipMethod);
        this.isGateway = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_IPGATEWAY, this.isGateway);
        this.isGN = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_GN, this.isGN);
        this.isITunes = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_ITUNES, this.isITunes);
        this.isLogin = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_LOGIN, this.isLogin);
        this.isOnline = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_ONLINE, this.isOnline);
        this.isPrinter = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_PRINTER, this.isPrinter);
        this.isWebServer = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_WEBSRV, this.isWebServer);
        this.isWL = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_WIFI, this.isWL);
        this.keeparp = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_KEEPARP, this.keeparp);
        this.mac = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_MAC, this.mac);
        this.macRepeat = jsonObjectToBool(jsonObject, JSON_MEMBER_CLIENT_MACREPEAT, this.macRepeat);
        this.name = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_NAME, this.name);
        this.nickName = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_NICK, this.nickName);
        this.opMode = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_MODE, this.opMode);
        this.qosLevel = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_QOSLVL, this.qosLevel);
        this.ROG = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_ROG, this.ROG);
        this.rssi = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RSSI, this.rssi);
        this.ssid = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_SSID, this.ssid);
        this.totalRx = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXTOTAL, this.totalRx);
        this.totalTx = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXTOTAL, this.totalTx);
        this.vendor = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_VENDOR, this.vendor);
        this.wlConnectTime = jsonObjectToString(jsonObject, JSON_MEMBER_CLIENT_CONNECTTIME, this.wlConnectTime);
        this.wtfast = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_WTFAST, this.wtfast);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public Integer getRX() {
        return this.curRx;
    }

    public Integer getTX() {
        return this.curTx;
    }

    public Integer getDefaultType() {
        return this.defaultType;
    }

    public String getDpiDevice() {
        return this.dpiDevice;
    }

    public String getDpiType() {
        return this.dpiType;
    }

    public String getIpFrom() {
        return this.from;
    }

    public String getGroup() {
        return this.group;
    }

    public String getInternetMode() {
        return this.internetMode;
    }

    public Boolean getInternetState() {
        return this.internetState;
    }

    public String getIP() {
        return this.ip;
    }

    public String getIpMethod() {
        return this.ipMethod;
    }

    public Boolean isGateway() {
        return this.isGateway;
    }

    public Boolean isGN() {
        return this.isGN;
    }

    public Boolean isITunes() {
        return this.isITunes;
    }

    public Boolean isLogin() {
        return this.isLogin;
    }

    public Boolean isOnline() {
        return this.isOnline;
    }

    public Boolean isPrinter() {
        return this.isPrinter;
    }

    public Boolean isWebServer() {
        return this.isWebServer;
    }

    public Integer isWL() {
        return this.isWL;
    }

    public Boolean isWiFiConnected() {
        return this.isWL > 0;
    }

    public String getKeepArp() {
        return this.keeparp;
    }

    public String getMac() {
        return this.mac;
    }

    public Boolean getMacRepeat() {
        return this.macRepeat;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {
        return this.nickName;
    }

    public Integer getOpMode() {
        return this.opMode;
    }

    public String getQosLevel() {
        return this.qosLevel;
    }

    public Integer getROG() {
        return this.ROG;
    }

    public Integer getRSSI() {
        return this.rssi;
    }

    public String getSSID() {
        return this.ssid;
    }

    public Integer getTotalRX() {
        return this.totalRx;
    }

    public Integer getTotalTX() {
        return this.totalTx;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getWlanConnectTime() {
        return this.wlConnectTime;
    }

    public Integer getWtFast() {
        return this.wtfast;
    }
}
