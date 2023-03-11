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
package org.openhab.binding.broadlink.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Device configuration for the supported Broadlink devices.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkDeviceConfiguration {
    private String ipAddress;
    private boolean staticIp;
    private int port;
    private String macAddress;
    private byte[] macAddressBytes;
    private int pollingInterval;
    private String mapFilename;
    private boolean ignoreFailedUpdates;
    private int deviceType;

    public BroadlinkDeviceConfiguration() {
        ipAddress = "";
        staticIp = true;
        macAddress = "";
        macAddressBytes = new byte[0];
        pollingInterval = 30;
        mapFilename = "broadlink.map";
        ignoreFailedUpdates = false;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isStaticIp() {
        return staticIp;
    }

    public void setStaticIp(boolean staticIp) {
        this.staticIp = staticIp;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setMacAddress(String macAddress) {
        this.macAddressBytes = new byte[0];
        this.macAddress = macAddress;
    }

    public byte[] getMacAddress() {
        if (macAddressBytes.length != 6) {
            macAddressBytes = new byte[6];
            String elements[] = macAddress.split(":");
            for (int i = 0; i < 6; i++) {
                String element = elements[i];
                macAddressBytes[i] = (byte) Integer.parseInt(element, 16);
            }
        }
        return macAddressBytes;
    }

    public String getMacAddressAsString() {
        return macAddress;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getMapFilename() {
        return mapFilename;
    }

    public void setMapFilename(String mapFilename) {
        this.mapFilename = mapFilename;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int newDeviceType) {
        this.deviceType = newDeviceType;
    }

    public void setIgnoreFailedUpdates(boolean ignore) {
        this.ignoreFailedUpdates = ignore;
    }

    public boolean isIgnoreFailedUpdates() {
        return this.ignoreFailedUpdates;
    }

    public String toString() {
        return (new StringBuilder("BroadlinkDeviceConfiguration [ipAddress=")).append(ipAddress).append(" (static: ")
                .append(staticIp).append("), port=").append(port).append(", macAddress=").append(macAddress)
                .append(", pollingInterval=").append(pollingInterval).append(", mapFilename=").append(mapFilename)
                .append("]").toString();
    }
}
