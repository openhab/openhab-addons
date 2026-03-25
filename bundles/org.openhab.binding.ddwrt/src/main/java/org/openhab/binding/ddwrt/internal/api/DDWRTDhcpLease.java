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
 * Data object representing a DHCP lease from dnsmasq.
 *
 * Lease file format (dnsmasq): {@code expiry mac ip hostname clientid}
 * Example: {@code 1740000000 aa:bb:cc:dd:ee:ff 192.168.1.100 myphone 01:aa:bb:cc:dd:ee:ff}
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTDhcpLease {

    private final String mac;
    private String hostname = "";
    private String ipAddress = "";
    private long expiry = 0;

    public DDWRTDhcpLease(String mac) {
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

    public long getExpiry() {
        return expiry;
    }

    // ---- Setters ----

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    @Override
    public String toString() {
        return "DDWRTDhcpLease{mac='" + mac + "', hostname='" + hostname + "', ip='" + ipAddress + "'}";
    }
}
