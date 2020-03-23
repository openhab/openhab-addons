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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanHostL3Connectivity} is the Java class used to map the "LanHostL3Connectivity"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class LanHostL3Connectivity {
    public static enum L3Af {
        UNKNOWN,
        @SerializedName("ipv4")
        IPV4,
        @SerializedName("ipv6")
        IPV6;
    }

    private String addr;
    private L3Af af = L3Af.UNKNOWN;
    private boolean active;
    private boolean reachable;
    private long lastActivity;
    private long lastTimeReachable;

    public String getAddr() {
        return addr;
    }

    public L3Af getAf() {
        return af;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public long getLastTimeReachable() {
        return lastTimeReachable;
    }
}
