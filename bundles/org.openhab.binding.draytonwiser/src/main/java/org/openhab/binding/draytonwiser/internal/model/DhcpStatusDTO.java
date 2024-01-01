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
public class DhcpStatusDTO {

    private String status;
    private Long leaseStartTime;
    private Long leaseTime;
    private String iPv4Address;
    private String iPv4SubnetMask;
    private String iPv4DefaultGateway;
    private String iPv4PrimaryDNS;
    private String iPv4SecondaryDNS;

    public String getStatus() {
        return status;
    }

    public Long getLeaseStartTime() {
        return leaseStartTime;
    }

    public Long getLeaseTime() {
        return leaseTime;
    }

    public String getIPv4Address() {
        return iPv4Address;
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
