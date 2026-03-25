/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Data object representing a wireless client associated with a DD-WRT network device.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTWirelessClient {

    private String mac;
    private String hostname = "";
    private String ipAddress = "";
    private String apMac = "";
    private String radioName = "";
    private String ssid = "";
    private String iface = "";
    private int snr = 0;
    private int signalDbm = 0;
    private int noiseDbm = 0;
    private String rxRate = "";
    private String txRate = "";
    private int channel = 0;
    private boolean online = false;
    private @Nullable Instant lastSeen;

    public DDWRTWirelessClient(String mac) {
        this.mac = Objects.requireNonNull(mac.toLowerCase().trim());
    }

    // ---- Getters ----

    public String getMac() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getApMac() {
        return apMac;
    }

    public String getRadioName() {
        return radioName;
    }

    public String getSsid() {
        return ssid;
    }

    public String getIface() {
        return iface;
    }

    public int getSnr() {
        return snr;
    }

    public int getSignalDbm() {
        return signalDbm;
    }

    public int getNoiseDbm() {
        return noiseDbm;
    }

    public String getRxRate() {
        return rxRate;
    }

    public String getTxRate() {
        return txRate;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isOnline() {
        return online;
    }

    public @Nullable Instant getLastSeen() {
        return lastSeen;
    }

    // ---- Setters ----

    public void setMac(String mac) {
        this.mac = Objects.requireNonNull(mac.toLowerCase().trim());
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setApMac(String apMac) {
        this.apMac = Objects.requireNonNull(apMac.toLowerCase().trim());
    }

    public void setRadioName(String radioName) {
        this.radioName = radioName;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setIface(String iface) {
        this.iface = iface;
    }

    public void setSnr(int snr) {
        this.snr = snr;
    }

    public void setSignalDbm(int signalDbm) {
        this.signalDbm = signalDbm;
    }

    public void setNoiseDbm(int noiseDbm) {
        this.noiseDbm = noiseDbm;
    }

    public void setRxRate(String rxRate) {
        this.rxRate = rxRate;
    }

    public void setTxRate(String txRate) {
        this.txRate = txRate;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.lastSeen = Instant.now();
        }
    }

    public void setLastSeen(@Nullable Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "DDWRTWirelessClient{mac='" + mac + "', hostname='" + hostname + "', ip='" + ipAddress + "', ap='"
                + apMac + "', snr=" + snr + ", online=" + online + "}";
    }
}
