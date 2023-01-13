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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LanAccessPoint} is the Java class used to map the "LanHostL3Connectivity" structure used by the Lan Hosts
 * Browser API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanAccessPoint implements WifiDeviceIntf {
    private @Nullable String mac;
    private @Nullable String type;
    private long rxBytes; // received bytes (from station to Freebox)
    private long txBytes; // transmitted bytes (from Freebox to station)
    private long txRate; // reception data rate (in bytes/s)
    private long rxRate; // transmission data rate (in bytes/s)
    private @Nullable String uid;
    private @Nullable String connectivityType;
    private @Nullable WifiInformation wifiInformation;

    public String getMac() {
        return Objects.requireNonNull(mac);
    }

    public String getType() {
        return Objects.requireNonNull(type);
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public long getTxBytes() {
        return txBytes;
    }

    @Override
    public long getTxRate() {
        return txRate;
    }

    @Override
    public long getRxRate() {
        return rxRate;
    }

    @Override
    public int getSignal() {
        return getWifiInformation().getSignal();
    }

    public String getUid() {
        return Objects.requireNonNull(uid);
    }

    public @Nullable String getConnectivityType() {
        return connectivityType;
    }

    public WifiInformation getWifiInformation() {
        return Objects.requireNonNull(wifiInformation);
    }

    @Override
    public @Nullable String getSsid() {
        return getWifiInformation().getSsid();
    }

}
