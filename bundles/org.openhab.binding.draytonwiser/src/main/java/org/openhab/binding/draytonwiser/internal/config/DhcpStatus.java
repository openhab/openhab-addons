/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DhcpStatus {

    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("LeaseStartTime")
    @Expose
    private Integer leaseStartTime;
    @SerializedName("LeaseTime")
    @Expose
    private Integer leaseTime;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLeaseStartTime() {
        return leaseStartTime;
    }

    public void setLeaseStartTime(Integer leaseStartTime) {
        this.leaseStartTime = leaseStartTime;
    }

    public Integer getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(Integer leaseTime) {
        this.leaseTime = leaseTime;
    }

    public String getIPv4Address() {
        return iPv4Address;
    }

    public void setIPv4Address(String iPv4Address) {
        this.iPv4Address = iPv4Address;
    }

    public String getIPv4SubnetMask() {
        return iPv4SubnetMask;
    }

    public void setIPv4SubnetMask(String iPv4SubnetMask) {
        this.iPv4SubnetMask = iPv4SubnetMask;
    }

    public String getIPv4DefaultGateway() {
        return iPv4DefaultGateway;
    }

    public void setIPv4DefaultGateway(String iPv4DefaultGateway) {
        this.iPv4DefaultGateway = iPv4DefaultGateway;
    }

    public String getIPv4PrimaryDNS() {
        return iPv4PrimaryDNS;
    }

    public void setIPv4PrimaryDNS(String iPv4PrimaryDNS) {
        this.iPv4PrimaryDNS = iPv4PrimaryDNS;
    }

    public String getIPv4SecondaryDNS() {
        return iPv4SecondaryDNS;
    }

    public void setIPv4SecondaryDNS(String iPv4SecondaryDNS) {
        this.iPv4SecondaryDNS = iPv4SecondaryDNS;
    }

}
