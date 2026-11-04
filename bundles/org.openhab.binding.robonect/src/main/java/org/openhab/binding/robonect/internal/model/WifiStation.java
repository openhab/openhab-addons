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

    /**
     * Reports whether the WLAN station is enabled.
     *
     * @return whether the WLAN station is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getEnable() {
        return enable;
    }

    /**
     * Returns the MAC address.
     *
     * @return the MAC address, or {@code null} if not reported
     */
    public @Nullable String getMac() {
        return mac;
    }

    /**
     * Returns the reported WLAN signal strength.
     *
     * @return the reported WLAN signal strength, or {@code null} if not reported
     */
    public @Nullable Integer getSignal() {
        return signal;
    }

    /**
     * Returns the SSID.
     *
     * @return the SSID, or {@code null} if not reported
     */
    public @Nullable String getSsid() {
        return ssid;
    }

    /**
     * Reports whether DHCP is enabled.
     *
     * @return whether DHCP is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getDhcp() {
        return dhcp;
    }

    /**
     * Reports whether gateway monitoring is enabled.
     *
     * @return whether gateway monitoring is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getPing() {
        return ping;
    }

    /**
     * Returns the gateway ping response time as text.
     *
     * @return the gateway ping response time as text, or {@code null} if not reported
     */
    public @Nullable String getPingtime() {
        return pingtime;
    }

    /**
     * Returns the station IP address.
     *
     * @return the station IP address, or {@code null} if not reported
     */
    public @Nullable String getIp() {
        return ip;
    }

    /**
     * Returns the station network mask.
     *
     * @return the station network mask, or {@code null} if not reported
     */
    public @Nullable String getNetmask() {
        return netmask;
    }

    /**
     * Returns the gateway IP address.
     *
     * @return the gateway IP address, or {@code null} if not reported
     */
    public @Nullable String getGateway() {
        return gateway;
    }
}
