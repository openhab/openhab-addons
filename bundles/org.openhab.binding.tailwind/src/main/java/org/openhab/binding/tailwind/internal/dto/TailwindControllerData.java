/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.dto;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TailwindControllerData} is the Java class used to map the JSON response to an TailWind API request.
 *
 * @author dschoepel - Initial contribution
 *
 */
public class TailwindControllerData implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /* --------- Controller information follows, then door data ------------------ */
    @SerializedName("result")
    private String result; // Result status "OK", "xxxx fail"

    @SerializedName("info")
    private String info; // If result is fail, info explaining the failure

    @SerializedName("product")
    private String product; // Model number (iQ3, light)

    @SerializedName("dev_id")
    private String devId; // MAC address in format "_8_d1_f9_12_2_ec_"

    @SerializedName("proto_ver")
    private String protoVer; // JSON protocol version number, 0.1 for current version, reserved for future use

    @SerializedName("door_num")
    private long doorNum; // Number of doors being controlled (1 -3)

    @SerializedName("night_mode_en")
    private long nightModeEn; // Night mode enabled (1-yes, 0-no)

    @SerializedName("fw_ver")
    private String fwVer; // Firmware version

    @SerializedName("led_brightness")
    private int ledBrightness; // LED Brightness (0 - 100)

    @SerializedName("router_rssi")
    private long routerRssi; // Wi-Fi signal strength

    @SerializedName("server_monitor")
    private Boolean serverMonitor; // Server connection monitoring enabled (firmware 10.61+, null on older firmware)

    @SerializedName("data")
    private TailwindDoorData data; // Object with door status details (door1, door2, door3)

    @SerializedName("notify")
    private TailwindNotification notify; // UPD notification when door state changes

    public String getResult() {
        return result;
    }

    public String getInfo() {
        return info;
    }

    public void setResult(String value) {
        this.result = value;
    }

    public void setInfo(String value) {
        this.info = value;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String value) {
        this.product = value;
    }

    public String getDevID() {
        return devId;
    }

    public void setDevID(String value) {
        this.devId = value;
    }

    public String getProtoVer() {
        return protoVer;
    }

    public void setProtoVer(String value) {
        this.protoVer = value;
    }

    public long getDoorNum() {
        return doorNum;
    }

    public void setDoorNum(long value) {
        this.doorNum = value;
    }

    public long getNightModeEn() {
        return nightModeEn;
    }

    public void setNightModeEn(long value) {
        this.nightModeEn = value;
    }

    public String getFwVer() {
        return fwVer;
    }

    public void setFwVer(String value) {
        this.fwVer = value;
    }

    public int getLEDBrightness() {
        return ledBrightness;
    }

    public void setLEDBrightness(int value) {
        this.ledBrightness = value;
    }

    public long getRouterRssi() {
        return routerRssi;
    }

    public void setRouterRssi(long value) {
        this.routerRssi = value;
    }

    public Boolean getServerMonitor() {
        return serverMonitor;
    }

    public void setServerMonitor(Boolean value) {
        this.serverMonitor = value;
    }

    public TailwindDoorData getDoorData() {
        return data;
    }

    public void setDoorData(TailwindDoorData value) {
        this.data = value;
    }

    public TailwindNotification getNotify() {
        return notify;
    }

    public void setNotify(TailwindNotification notify) {
        this.notify = notify;
    }

    @Override
    public String toString() {
        return "ControllerData [result=" + result + ", info=" + info + ", product=" + product + ", devId=" + devId
                + ", protoVer=" + protoVer + ", doorNum=" + doorNum + ", nightModeEn=" + nightModeEn + ", fwVer="
                + fwVer + ", ledBrightness=" + ledBrightness + ", routerRssi=" + routerRssi + ", serverMonitor="
                + serverMonitor + ", data=" + data + ", notify=" + notify + "]";
    }
}
