/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxConnectionStatus} is the Java class used to map the "ConnectionStatus"
 * structure used by the connection API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxConnectionStatus {
    private String state;
    private String type;
    private String media;
    private String ipv4;
    private String ipv6;
    private long rateUp;
    private long rateDown;
    private long bandwidthUp;
    private long bandwidthDown;
    private long bytesUp;
    private long bytesDown;
    private Long[] ipv4PortRange;

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getMedia() {
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
