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
package org.openhab.binding.airvisualnode.internal.json.airvisual;

/**
 * Status data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class Status {

    private String appVersion;
    private int battery;
    private long datetime;
    private String model;
    private String sensorPm25Serial;
    private int syncTime;
    private String systemVersion;
    private int usedMemory;
    private int wifiStrength;

    public Status(String appVersion, int battery, long datetime, String model, String sensorPm25Serial, int syncTime,
            String systemVersion, int usedMemory, int wifiStrength) {
        this.appVersion = appVersion;
        this.battery = battery;
        this.datetime = datetime;
        this.model = model;
        this.sensorPm25Serial = sensorPm25Serial;
        this.syncTime = syncTime;
        this.systemVersion = systemVersion;
        this.usedMemory = usedMemory;
        this.wifiStrength = wifiStrength;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSensorPm25Serial() {
        return sensorPm25Serial;
    }

    public void setSensorPm25Serial(String sensorPm25Serial) {
        this.sensorPm25Serial = sensorPm25Serial;
    }

    public int getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(int syncTime) {
        this.syncTime = syncTime;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public int getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(int usedMemory) {
        this.usedMemory = usedMemory;
    }

    public int getWifiStrength() {
        return wifiStrength;
    }

    public void setWifiStrength(int wifiStrength) {
        this.wifiStrength = wifiStrength;
    }
}
