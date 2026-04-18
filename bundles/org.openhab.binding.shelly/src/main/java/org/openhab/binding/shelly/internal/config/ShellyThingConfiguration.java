/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellyThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingConfiguration {
    private String deviceIp = ""; // IP address of the device
    private String deviceAddress = ""; // IP address or MAC address for BLU devices
    private String userId = ""; // userid for http basic auth
    private String password = ""; // password for http basic auth

    private int updateInterval = 60; // schedule interval for the update job
    private int lowBattery = 15; // threshold for battery value
    private boolean brightnessAutoOn = true; // true: turn on device if brightness > 0 is set

    private int favoriteUP = 0; // Roller position favorite when control channel receives ON, 0=none
    private int favoriteDOWN = 0; // Roller position favorite when control channel receives OFF, 0=none

    // Gen1
    private boolean eventsButton = false; // true: register for Relay btn_xxx events
    private boolean eventsSwitch = true; // true: register for device out_xxx events
    private boolean eventsPush = true; // true: register for short/long push events
    private boolean eventsRoller = true; // true: register for short/long push events
    private boolean eventsSensorReport = true; // true: register for sensor events
    private boolean eventsCoIoT = false; // true: use CoIoT events (based on COAP)

    // Gen2
    private Boolean enableBluGateway = false;
    private Boolean enableRangeExtender = true;

    public String getDeviceIp() {
        return deviceIp;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public int getLowBattery() {
        return lowBattery;
    }

    public boolean getBrightnessAutoOn() {
        return brightnessAutoOn;
    }

    public int getFavoriteUP() {
        return favoriteUP;
    }

    public int getFavoriteDOWN() {
        return favoriteDOWN;
    }

    public boolean getEventsButton() {
        return eventsButton;
    }

    public boolean getEventsSwitch() {
        return eventsSwitch;
    }

    public boolean getEventsPush() {
        return eventsPush;
    }

    public boolean getEventsRoller() {
        return eventsRoller;
    }

    public boolean getEventsSensorReport() {
        return eventsSensorReport;
    }

    public boolean getEventsCoIoT() {
        return eventsCoIoT;
    }

    public boolean getEnableBluGateway() {
        return enableBluGateway;
    }

    public boolean getEnableRangeExtender() {
        return enableRangeExtender;
    }

    @Override
    public String toString() {
        return "Device address=" + deviceAddress + ", HTTP user/password=" + userId + "/"
                + (password.isEmpty() ? "<none>" : "***") + ", update interval=" + updateInterval + "\n"
                + "Events: Button: " + eventsButton + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush
                + ", Roller: " + eventsRoller + "Sensor: " + eventsSensorReport + ", CoIoT: " + eventsCoIoT + "\n"
                + "Blu Gateway=" + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
