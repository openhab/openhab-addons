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
package org.openhab.binding.draytonwiser.internal.model;

import java.util.List;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class StationDTO {

    private Boolean enabled;
    private String sSID;
    private String securityMode;
    private Long scanSlotTime;
    private Integer scanSlots;
    private NetworkInterfaceDTO networkInterface;
    private String connectionStatus;
    private DhcpStatusDTO dhcpStatus;
    private Boolean scanning;
    private List<DetectedAccessPointDTO> detectedAccessPoints;
    private Integer connectionFailures;
    private String mdnsHostname;
    private String macAddress;
    private RssiDTO rSSI;
    private Integer channel;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSSID() {
        return sSID;
    }

    public void setSSID(final String sSID) {
        this.sSID = sSID;
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(final String securityMode) {
        this.securityMode = securityMode;
    }

    public Long getScanSlotTime() {
        return scanSlotTime;
    }

    public void setScanSlotTime(final Long scanSlotTime) {
        this.scanSlotTime = scanSlotTime;
    }

    public Integer getScanSlots() {
        return scanSlots;
    }

    public void setScanSlots(final Integer scanSlots) {
        this.scanSlots = scanSlots;
    }

    public NetworkInterfaceDTO getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(final NetworkInterfaceDTO networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(final String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public DhcpStatusDTO getDhcpStatus() {
        return dhcpStatus;
    }

    public void setDhcpStatus(final DhcpStatusDTO dhcpStatus) {
        this.dhcpStatus = dhcpStatus;
    }

    public Boolean getScanning() {
        return scanning;
    }

    public void setScanning(final Boolean scanning) {
        this.scanning = scanning;
    }

    public List<DetectedAccessPointDTO> getDetectedAccessPoints() {
        return detectedAccessPoints;
    }

    public void setDetectedAccessPoints(final List<DetectedAccessPointDTO> detectedAccessPoints) {
        this.detectedAccessPoints = detectedAccessPoints;
    }

    public Integer getConnectionFailures() {
        return connectionFailures;
    }

    public void setConnectionFailures(final Integer connectionFailures) {
        this.connectionFailures = connectionFailures;
    }

    public String getMdnsHostname() {
        return mdnsHostname;
    }

    public void setMdnsHostname(final String mdnsHostname) {
        this.mdnsHostname = mdnsHostname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public RssiDTO getRSSI() {
        return rSSI;
    }

    public void setRSSI(final RssiDTO rSSI) {
        this.rSSI = rSSI;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(final Integer channel) {
        this.channel = channel;
    }
}
