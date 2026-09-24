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

    public @Nullable Boolean getEnable() {
        return enable;
    }

    public @Nullable String getMac() {
        return mac;
    }

    public @Nullable Boolean getHidden() {
        return hidden;
    }

    public @Nullable String getSsid() {
        return ssid;
    }

    public @Nullable Integer getChannel() {
        return channel;
    }

    public @Nullable String getEncryption() {
        return encryption;
    }

    public @Nullable Integer getMaxconn() {
        return maxconn;
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

    public @Nullable Dhcp getDhcp() {
        return dhcp;
    }
}
