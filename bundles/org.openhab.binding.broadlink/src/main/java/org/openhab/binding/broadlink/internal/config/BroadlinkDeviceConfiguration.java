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
package org.openhab.binding.broadlink.internal.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Device configuration for the supported Broadlink devices.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkDeviceConfiguration {
    private String ipAddress = "";
    private boolean staticIp = true;
    private int port;
    private String macAddress = "";
    private byte[] macAddressBytes = new byte[0];
    private int pollingInterval = 30;
    private String nameOfCommandToLearn = "DEVICE_ON";
    private int deviceType;

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

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int newDeviceType) {
        this.deviceType = newDeviceType;
    }

    public String getNameOfCommandToLearn() {
        return nameOfCommandToLearn;
    }

    public void setNameOfCommandToLearn(String nameOfCommandToLearn) {
        this.nameOfCommandToLearn = nameOfCommandToLearn;
    }

    public String isValidConfiguration() {
        if (ipAddress.length() == 0) {
            return "Not a valid IP address";
        }
        if (port == 0) {
            return "Port cannot be 0";
        }
        if (macAddress.isBlank()) {
            return "No MAC address defined";
        }
        // Regex to check valid MAC address
        String regex = "^([0-9A-Fa-f]{2}[:-])" + "{5}([0-9A-Fa-f]{2})|" + "([0-9a-fA-F]{4}\\." + "[0-9a-fA-F]{4}\\."
                + "[0-9a-fA-F]{4})$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        // Find match between given string and regular expression using Pattern.matcher()
        Matcher m = p.matcher(macAddress);
        // Return if the string matched the regular expression
        if (!m.matches()) {
            return "MAC address is not of the form XX:XX:XX:XX:XX:XX";
        }
        if (pollingInterval == 0) {
            return "Polling interval cannot be 0";
        }
        if (nameOfCommandToLearn.isBlank()) {
            return "Name of command to learn needs to be defined";
        }
        if (deviceType == 0) {
            return "Device type cannot be 0";
        }

        return "";
    }

    @Override
    public String toString() {
        return (new StringBuilder("BroadlinkDeviceConfiguration [ipAddress=")).append(ipAddress).append(" (static: ")
                .append(staticIp).append("), port=").append(port).append(", macAddress=").append(macAddress)
                .append(", pollingInterval=").append(pollingInterval).append(", nameOfCommandToLearn=")
                .append(nameOfCommandToLearn).append("]").toString();
    }
}
