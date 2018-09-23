/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal;

import org.openhab.binding.sonyps4.internal.discovery.SonyPS4Discovery;

/**
 * The {@link SonyPS4Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class SonyPS4Configuration {

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipAddress}.
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipPort}.
     */
    public static final String IP_PORT = "ipPort";

    /**
     * IP-address of PS4.
     */
    private String ipAddress;

    /**
     * IP-port of PS4.
     */
    private String ipPort;

    /**
     * host-id of PS4.
     */
    private String hostId;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    @Override
    public String toString() {
        return "IP" + ipAddress + ", Port" + ipPort + ", HostId" + hostId + ".";
    }
}
