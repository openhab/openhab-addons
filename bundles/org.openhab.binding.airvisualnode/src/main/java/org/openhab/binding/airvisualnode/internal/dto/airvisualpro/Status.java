/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airvisualnode.internal.dto.airvisualpro;

/**
 * Status data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class Status {

    private String appVersion;
    private int battery;
    private long datetime;
    private String deviceName;
    private String ipAddress;
    private String macAddress;
    private String model;
    private SensorLife sensorLife;
    private String sensorPm25Serial;
    private int syncTime;
    private String systemVersion;
    private int usedMemory;
    private int wifiStrength;

    public Status(String appVersion, int battery, long datetime, String deviceName, String ipAddress, String macAddress,
            String model, SensorLife sensorLife, String sensorPm25Serial, int syncTime, String systemVersion,
            int usedMemory, int wifiStrength) {
        this.appVersion = appVersion;
        this.battery = battery;
        this.datetime = datetime;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.model = model;
        this.sensorLife = sensorLife;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public SensorLife getSensorLife() {
        return sensorLife;
    }

    public void setSensorLife(SensorLife sensorLife) {
        this.sensorLife = sensorLife;
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
