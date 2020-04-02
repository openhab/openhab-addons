/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ConnectionStatus} is the Java class used to map the "ConnectionStatus"
 * structure used by the connection API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class ConnectionStatus {

    public static enum State {
        UNKNOWN,
        @SerializedName("going_up")
        GOING_UP,
        @SerializedName("up")
        UP,
        @SerializedName("going_down")
        GOING_DOWN,
        @SerializedName("down")
        DOWN;
    }

    public static enum Type {
        UNKNOWN,
        @SerializedName("ethernet")
        ETHERNET,
        @SerializedName("rfc2684")
        RFC2684,
        @SerializedName("pppoatm")
        PPOATM;
    }

    public static enum Media {
        UNKNOWN,
        @SerializedName("ftth")
        FTTH,
        @SerializedName("xdsl")
        XDSL;
    }

    private State state = State.UNKNOWN;
    private Type type = Type.UNKNOWN;
    private Media media = Media.UNKNOWN;
    private String ipv4 = "";
    private String ipv6 = "";
    private long rateUp; // current upload rate in byte/s
    private long rateDown; // current download rate in byte/s
    private long bandwidthUp; // available upload bandwidth in bit/s
    private long bandwidthDown; // available download bandwidth in bit/s
    private long bytesUp; // total uploaded bytes since last connection
    private long bytesDown; // total downloaded bytes since last connection
    // Some customers share the same IPv4 and each customer is then assigned a port range. The first value is the first
    // port of the assigned range and the second value is the last port (inclusive).
    private long[] ipv4PortRange = new long[0];

    public State getState() {
        return state;
    }

    public Type getType() {
        return type;
    }

    public Media getMedia() {
        return media;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public long getRateUp() {
        return rateUp;
    }

    public long getRateDown() {
        return rateDown;
    }

    public long getBandwidthUp() {
        return bandwidthUp;
    }

    public long getBandwidthDown() {
        return bandwidthDown;
    }

    public long getBytesUp() {
        return bytesUp;
    }

    public long getBytesDown() {
        return bytesDown;
    }

    public Long getIpvPortMin() {
        return ipv4PortRange[0];
    }

    public Long getIpvPortMax() {
        return ipv4PortRange[1];
    }
}
