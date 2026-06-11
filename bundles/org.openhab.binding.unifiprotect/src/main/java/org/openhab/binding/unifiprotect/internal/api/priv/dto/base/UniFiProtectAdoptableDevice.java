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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.base;

import java.time.Instant;
import java.util.UUID;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.StateType;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for adoptable UniFi Protect devices
 * Includes adoption, connection state, and network connection properties
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class UniFiProtectAdoptableDevice extends UniFiProtectDevice {

    public StateType state;
    public String connectionHost;
    public Instant connectedSince;
    public String latestFirmwareVersion;
    public String firmwareBuild;
    public Boolean isAdopting;
    public Boolean isAdopted;
    public Boolean isAdoptedByOther;
    public Boolean isProvisioned;
    public Boolean isRebooting;
    public Boolean canAdopt;
    public Boolean isAttemptingToConnect;
    public Boolean isConnected;
    public String marketName;
    public String nvrMac;
    public UUID guid;
    public Boolean isRestoring;
    public Instant lastDisconnect;
    public UUID anonymousDeviceId;
    public WiredConnectionState wiredConnectionState;
    public WifiConnectionState wifiConnectionState;
    public BluetoothConnectionState bluetoothConnectionState;

    @SerializedName("bridge")
    public String bridgeId;

    @SerializedName("isDownloadingFW")
    public Boolean isDownloadingFirmware;

    // Nested connection state classes

    public static class WiredConnectionState {
        public Double phyRate;
    }

    public static class WifiConnectionState {
        public Integer signalQuality;
        public Integer signalStrength;
        public Double phyRate;
        public Integer channel;
        public Integer frequency;
        public String ssid;
        public String bssid;
        public Double txRate;
        public String apName;
        public String experience;
        public String connectivity;
    }

    public static class BluetoothConnectionState {
        public Integer signalQuality;
        public Integer signalStrength;
        public Double experienceScore;
    }

    public boolean isAdoptedByUs() {
        return Boolean.TRUE.equals(isAdopted) && !Boolean.TRUE.equals(isAdoptedByOther);
    }

    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (marketName != null && !marketName.isEmpty()) {
            return marketName;
        }
        return type;
    }
}
