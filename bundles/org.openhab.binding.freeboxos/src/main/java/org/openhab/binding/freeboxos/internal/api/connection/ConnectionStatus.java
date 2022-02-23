/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ConnectionStatus} is the Java class used to map the "ConnectionStatus"
 * structure used by the connection API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionStatus {
    public static class ConnectionStatusResponse extends Response<ConnectionStatus> {
    }

    public static enum State {
        @SerializedName("going_up")
        GOING_UP,
        @SerializedName("up")
        UP,
        @SerializedName("going_down")
        GOING_DOWN,
        @SerializedName("down")
        DOWN;
    }

    private @NonNullByDefault({}) State state;
    private @NonNullByDefault({}) String ipv4;
    private long rateUp; // current upload rate in byte/s
    private long rateDown; // current download rate in byte/s
    private long bandwidthUp; // available upload bandwidth in bit/s
    private long bandwidthDown; // available download bandwidth in bit/s
    private long bytesUp; // total uploaded bytes since last connection
    private long bytesDown; // total downloaded bytes since last connection

    public State getState() {
        return state;
    }

    public String getIpv4() {
        return ipv4;
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
}
