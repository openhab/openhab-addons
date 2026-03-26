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
 * The {@link ShellyThingBasicConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingBasicConfig {
    protected String deviceIp = ""; // ip address of thedevice
    protected String deviceAddress = ""; // IP address or MAC address for BLU devices

    // All access must be guarded by "this"
    protected String userId = ""; // userid for http basic auth

    // All access must be guarded by "this"
    protected String password = ""; // password for http basic auth

    protected int updateInterval = 60; // schedule interval for the update job
    protected int lowBattery = 15; // threshold for battery value
    protected boolean brightnessAutoOn = true; // true: turn on device if brightness > 0 is set

    protected int favoriteUP = 0; // Roller position favorite when control channel receives ON, 0=none
    protected int favoriteDOWN = 0; // Roller position favorite when control channel receives ON, 0=none

    // Gen1
    // All access must be guarded by "this"
    protected boolean eventsButton = false; // true: register for Relay btn_xxx events
    protected boolean eventsSwitch = true; // true: register for device out_xxx events
    protected boolean eventsPush = true; // true: register for short/long push events
    protected boolean eventsRoller = true; // true: register for short/long push events
    protected boolean eventsSensorReport = true; // true: register for sensor events
    protected boolean eventsCoIoT = false; // true: use CoIoT events (based on COAP)

    // Gen2
    protected Boolean enableBluGateway = false;
    protected Boolean enableRangeExtender = true;

    public String getDeviceIp() {
        String value = deviceIp;
        return value;
    }

    public String getDeviceAddress() {
        String value = deviceAddress;
        return value;
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

    public boolean getEnableBluGateway() {
        return enableBluGateway;
    }

    public boolean getEnableEnableRangeExtender() {
        return enableRangeExtender;
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

    /*
     * Those getter/setter need synchronization
     */
    public synchronized String getUserId() {
        String value = userId;
        return value;
    }

    public synchronized void setUserId(String userId) {
        this.userId = userId;
    }

    public synchronized String getPassword() {
        String value = password;
        return value;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized void disableGen1Events() {
        eventsCoIoT = true;
        eventsSwitch = false;
        eventsButton = false;
        eventsPush = false;
        eventsRoller = false;
        eventsSensorReport = false;
    }
}
