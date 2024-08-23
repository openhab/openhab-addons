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
package org.openhab.binding.bigassfan.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BigAssFanDevice} is responsible for storing information about a fan.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class BigAssFanDevice {
    /**
     * Name of device (e.g. Master Bedroom Fan)
     */
    private String label = "";

    /**
     * IP address of the device extracted from UDP packet
     */
    private String ipAddress = "";

    /**
     * MAC address of the device extracted from discovery message
     */
    private String macAddress = "";

    /**
     * Type of device extracted from discovery message (e.g. FAN or SWITCH)
     */
    private String type = "";

    /**
     * Model of device extracted from discovery message (e.g. HSERIES)
     */
    private String model = "";

    /**
     * The raw discovery message
     */
    private String discoveryMessage = "";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDiscoveryMessage() {
        return discoveryMessage;
    }

    public void setDiscoveryMessage(String discoveryMessage) {
        this.discoveryMessage = discoveryMessage;
    }

    public boolean isFan() {
        return type.toUpperCase().contains("FAN") ? true : false;
    }

    public boolean isSwitch() {
        return type.toUpperCase().contains("SWITCH") ? true : false;
    }

    public boolean isLight() {
        return type.toUpperCase().contains("LIGHT") ? true : false;
    }

    public void reset() {
        label = "";
        ipAddress = "";
        macAddress = "";
        type = "";
        model = "";
        discoveryMessage = "";
    }

    @Override
    public String toString() {
        return "BigAssFanDevice{label=" + label + ", ipAddress=" + ipAddress + ", macAddress=" + macAddress + ", model="
                + model + ", type=" + type + "}";
    }
}
