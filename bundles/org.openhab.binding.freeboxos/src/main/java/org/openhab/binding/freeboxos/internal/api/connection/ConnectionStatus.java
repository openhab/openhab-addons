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
package org.openhab.binding.freeboxos.internal.api.connection;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ConnectionMedia;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ConnectionState;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ConnectionType;

/**
 * The {@link ConnectionStatus} is the Java class used to map the "ConnectionStatus" structure used by the connection
 * API
 *
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionStatus {
    private ConnectionState state = ConnectionState.UNKNOWN;
    private ConnectionType type = ConnectionType.UNKNOWN;
    private ConnectionMedia media = ConnectionMedia.UNKNOWN;
    private @Nullable String ipv4;
    private @Nullable String ipv6;
    private long rateUp; // current upload rate in byte/s
    private long rateDown; // current download rate in byte/s
    private long bandwidthUp; // available upload bandwidth in bit/s
    private long bandwidthDown; // available download bandwidth in bit/s
    private long bytesUp; // total uploaded bytes since last connection
    private long bytesDown; // total downloaded bytes since last connection
    private List<Integer> ipv4PortRange = List.of();

    public ConnectionState getState() {
        return state;
    }

    public @Nullable String getIpv4() { // This can be null if state is not up
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

    public ConnectionType getType() {
        return type;
    }

    public ConnectionMedia getMedia() {
        return media;
    }

    public @Nullable String getIpv6() {
        return ipv6;
    }

    public List<Integer> getIpv4PortRange() {
        return ipv4PortRange;
    }
}
