/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal.model;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class NetworkInterfaceDTO {

    private String interfaceName;
    private String hostName;
    private String dhcpMode;
    private String iPv4HostAddress;
    private String iPv4SubnetMask;
    private String iPv4DefaultGateway;
    private String iPv4PrimaryDNS;
    private String iPv4SecondaryDNS;

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDhcpMode() {
        return dhcpMode;
    }

    public String getIPv4HostAddress() {
        return iPv4HostAddress;
    }

    public String getIPv4SubnetMask() {
        return iPv4SubnetMask;
    }

    public String getIPv4DefaultGateway() {
        return iPv4DefaultGateway;
    }

    public String getIPv4PrimaryDNS() {
        return iPv4PrimaryDNS;
    }

    public String getIPv4SecondaryDNS() {
        return iPv4SecondaryDNS;
    }
}
