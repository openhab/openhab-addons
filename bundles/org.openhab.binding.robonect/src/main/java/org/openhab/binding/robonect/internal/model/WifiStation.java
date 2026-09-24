/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/** WLAN station state returned by Robonect. Credentials are deliberately not modeled. */
public class WifiStation {
    private @Nullable Boolean enable;
    private @Nullable String mac;
    private @Nullable Integer signal;
    private @Nullable String ssid;
    private @Nullable Boolean dhcp;
    private @Nullable Boolean ping;
    private @Nullable String pingtime;
    private @Nullable String ip;
    private @Nullable String netmask;
    private @Nullable String gateway;

    public @Nullable Boolean getEnable() {
        return enable;
    }

    public @Nullable String getMac() {
        return mac;
    }

    public @Nullable Integer getSignal() {
        return signal;
    }

    public @Nullable String getSsid() {
        return ssid;
    }

    public @Nullable Boolean getDhcp() {
        return dhcp;
    }

    public @Nullable Boolean getPing() {
        return ping;
    }

    public @Nullable String getPingtime() {
        return pingtime;
    }

    public @Nullable String getIp() {
        return ip;
    }

    public @Nullable String getNetmask() {
        return netmask;
    }

    public @Nullable String getGateway() {
        return gateway;
    }
}
