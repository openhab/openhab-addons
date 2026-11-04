/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/** Optional access-point state returned by Robonect. Credentials are deliberately not modeled. */
public class AccessPoint {
    private @Nullable Boolean enable;
    private @Nullable String mac;
    private @Nullable Boolean hidden;
    private @Nullable String ssid;
    private @Nullable Integer channel;
    private @Nullable String encryption;
    private @Nullable Integer maxconn;
    private @Nullable String ip;
    private @Nullable String netmask;
    private @Nullable String gateway;
    private @Nullable Dhcp dhcp;

    /**
     * Reports whether the access point is enabled.
     *
     * @return whether the access point is enabled, or {@code null} if absent
     */
    public @Nullable Boolean getEnable() {
        return enable;
    }

    /**
     * Returns the access-point MAC address.
     *
     * @return the access-point MAC address, or {@code null} if absent
     */
    public @Nullable String getMac() {
        return mac;
    }

    /**
     * Reports whether the access-point network is hidden.
     *
     * @return whether the access-point network is hidden, or {@code null} if absent
     */
    public @Nullable Boolean getHidden() {
        return hidden;
    }

    /**
     * Returns the access-point network name.
     *
     * @return the access-point network name, or {@code null} if absent
     */
    public @Nullable String getSsid() {
        return ssid;
    }

    /**
     * Returns the access-point radio channel.
     *
     * @return the access-point radio channel, or {@code null} if absent
     */
    public @Nullable Integer getChannel() {
        return channel;
    }

    /**
     * Returns the access-point encryption mode.
     *
     * @return the access-point encryption mode, or {@code null} if absent
     */
    public @Nullable String getEncryption() {
        return encryption;
    }

    /**
     * Returns the maximum number of access-point clients.
     *
     * @return the maximum number of access-point clients, or {@code null} if absent
     */
    public @Nullable Integer getMaxconn() {
        return maxconn;
    }

    /**
     * Returns the access-point IP address.
     *
     * @return the access-point IP address, or {@code null} if absent
     */
    public @Nullable String getIp() {
        return ip;
    }

    /**
     * Returns the access-point network mask.
     *
     * @return the access-point network mask, or {@code null} if absent
     */
    public @Nullable String getNetmask() {
        return netmask;
    }

    /**
     * Returns the access-point gateway.
     *
     * @return the access-point gateway, or {@code null} if absent
     */
    public @Nullable String getGateway() {
        return gateway;
    }

    /**
     * Returns the access-point DHCP configuration.
     *
     * @return the access-point DHCP configuration, or {@code null} if absent
     */
    public @Nullable Dhcp getDhcp() {
        return dhcp;
    }
}
