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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for wifi.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Wifi {
    private Boolean active;

    /*
     * API sometimes calls this field 'mac' and other times calls it 'address'.
     * Additionally, Gson uses fields only (not methods). Therefore, if use the
     * same instance of this class to read one value and then try to write the
     * other without calling the setter, it won't work (the other value will be
     * null).
     */
    private String mac;
    private String address;

    private Boolean available;
    private String encryption;

    /*
     * API sometimes calls this field 'ssid' and other times calls it 'essid'.
     * Additionally, Gson uses fields only (not methods). Therefore, if use the
     * same instance of this class to read one value and then try to write the
     * other without calling the setter, it won't work (the other value will be
     * null).
     */
    private String ssid;
    private String essid;

    /*
     * API sometimes calls this field 'ip' and other times calls it 'ipv4'.
     * Additionally, Gson uses fields only (not methods). Therefore, if use the
     * same instance of this class to read one value and then try to write the
     * other without calling the setter, it won't work (the other value will be
     * null).
     */
    private String ip;
    private String ipv4;

    private String mode;
    private String netmask;

    /*
     * API sometimes calls this field 'signal_strength' and other times calls it
     * 'strength'. Additionally, Gson uses fields only (not methods). Therefore,
     * if use the same instance of this class to read one value and then try to
     * write the other without calling the setter, it won't work (the other
     * value will be null).
     */
    private Integer signalStrength;
    private Integer strength;

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Wifi withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public String getMac() {
        return mac == null ? address : mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
        this.address = mac;
    }

    public Wifi withMac(String mac) {
        setMac(mac);
        return this;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Wifi withAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public Wifi withEncryption(String encryption) {
        this.encryption = encryption;
        return this;
    }

    public String getSsid() {
        return ssid == null ? essid : ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
        this.essid = ssid;
    }

    public Wifi withSsid(String ssid) {
        setSsid(ssid);
        return this;
    }

    public String getIp() {
        return ip == null ? ipv4 : ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        this.ipv4 = ip;
    }

    public Wifi withIp(String ip) {
        setIp(ip);
        return this;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Wifi withMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public Wifi withNetmask(String netmask) {
        this.netmask = netmask;
        return this;
    }

    public Integer getSignalStrength() {
        return signalStrength == null ? strength : signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
        this.strength = signalStrength;
    }

    public Wifi withSignalStrength(Integer signalStrength) {
        setSignalStrength(signalStrength);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Wifi [active=");
        builder.append(active);
        builder.append(", mac=");
        builder.append(getMac());
        builder.append(", available=");
        builder.append(available);
        builder.append(", encryption=");
        builder.append(encryption);
        builder.append(", ssid=");
        builder.append(getSsid());
        builder.append(", ip=");
        builder.append(getIp());
        builder.append(", mode=");
        builder.append(mode);
        builder.append(", netmask=");
        builder.append(netmask);
        builder.append(", signalStrength=");
        builder.append(getSignalStrength());
        builder.append("]");
        return builder.toString();
    }
}
