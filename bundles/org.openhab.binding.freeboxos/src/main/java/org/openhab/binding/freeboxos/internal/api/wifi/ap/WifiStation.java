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

import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.StationState;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;

/**
 * The {@link WifiStation} is the Java class used to map the "SwitchStatus" structure used by the response of the
 * switch status API
 *
 * https://dev.freebox.fr/sdk/os/switch/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiStation implements WifiDeviceIntf {
    private @Nullable String id;
    private @Nullable String mac;
    private @Nullable String bssid;
    private @Nullable String hostname;
    private @Nullable LanHost host;
    private StationState state = StationState.UNKNOWN;
    private int inactive;
    private int connDuration;
    private long rxBytes; // received bytes (from station to Freebox)
    private long txBytes; // transmitted bytes (from Freebox to station)
    private long txRate; // reception data rate (in bytes/s)
    private long rxRate; // transmission data rate (in bytes/s)
    private int signal; // signal attenuation (in dB)

    public String getId() {
        return Objects.requireNonNull(id);
    }

    public String getMac() {
        return Objects.requireNonNull(mac).toLowerCase();
    }

    public String getBssid() {
        return Objects.requireNonNull(bssid);
    }

    public @Nullable String getHostname() {
        return hostname;
    }

    public LanHost getHost() {
        return Objects.requireNonNull(host);
    }

    public StationState getState() {
        return state;
    }

    public int getInactive() {
        return inactive;
    }

    public int getConnDuration() {
        return connDuration;
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
        return signal;
    }

    @Override
    public @Nullable String getSsid() {
        LanAccessPoint accessPoint = getHost().getAccessPoint();
        if (accessPoint != null) {
            return accessPoint.getSsid();
        }
        return null;
    }

    public @Nullable ZonedDateTime getLastSeen() {
        return getHost().getLastSeen();
    }
}
