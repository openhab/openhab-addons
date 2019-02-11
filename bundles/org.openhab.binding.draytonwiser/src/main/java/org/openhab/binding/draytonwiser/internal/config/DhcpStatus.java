/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class DhcpStatus {

    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("LeaseStartTime")
    @Expose
    private Long leaseStartTime;
    @SerializedName("LeaseTime")
    @Expose
    private Long leaseTime;
    @SerializedName("IPv4Address")
    @Expose
    private String iPv4Address;
    @SerializedName("IPv4SubnetMask")
    @Expose
    private String iPv4SubnetMask;
    @SerializedName("IPv4DefaultGateway")
    @Expose
    private String iPv4DefaultGateway;
    @SerializedName("IPv4PrimaryDNS")
    @Expose
    private String iPv4PrimaryDNS;
    @SerializedName("IPv4SecondaryDNS")
    @Expose
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
