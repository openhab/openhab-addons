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

/**
 * @author Andrew Schofield - Initial contribution
 */
public class NetworkInterface {

    @SerializedName("InterfaceName")
    @Expose
    private String interfaceName;
    @SerializedName("HostName")
    @Expose
    private String hostName;
    @SerializedName("DhcpMode")
    @Expose
    private String dhcpMode;
    @SerializedName("IPv4HostAddress")
    @Expose
    private String iPv4HostAddress;
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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDhcpMode() {
        return dhcpMode;
    }

    public void setDhcpMode(String dhcpMode) {
        this.dhcpMode = dhcpMode;
    }

    public String getIPv4HostAddress() {
        return iPv4HostAddress;
    }

    public void setIPv4HostAddress(String iPv4HostAddress) {
        this.iPv4HostAddress = iPv4HostAddress;
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
