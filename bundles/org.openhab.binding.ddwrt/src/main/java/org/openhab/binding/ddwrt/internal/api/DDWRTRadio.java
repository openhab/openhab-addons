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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data object representing a wireless radio interface on a DD-WRT device.
 *
 * The interfaceId is the unique key: {@code <deviceMac>:<ifaceName>} (e.g. {@code aa:bb:cc:dd:ee:ff:ath0}).
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTRadio {

    private String interfaceId;
    private String ifaceName;
    private String parentDeviceMac;
    private String ssid = "";
    private int channel = 0;
    private String mode = "";
    private boolean enabled = false;
    private int clientCount = 0;

    public DDWRTRadio(String parentDeviceMac, String ifaceName) {
        this.parentDeviceMac = Objects.requireNonNull(parentDeviceMac.toLowerCase().trim());
        this.ifaceName = ifaceName;
        this.interfaceId = this.parentDeviceMac + ":" + ifaceName;
    }

    // ---- Getters ----

    public String getInterfaceId() {
        return interfaceId;
    }

    public String getIfaceName() {
        return ifaceName;
    }

    public String getParentDeviceMac() {
        return parentDeviceMac;
    }

    public String getSsid() {
        return ssid;
    }

    public int getChannel() {
        return channel;
    }

    public String getMode() {
        return mode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getClientCount() {
        return clientCount;
    }

    // ---- Setters ----

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    @Override
    public String toString() {
        return "DDWRTRadio{id='" + interfaceId + "', ssid='" + ssid + "', ch=" + channel + ", enabled=" + enabled
                + ", clients=" + clientCount + "}";
    }
}
