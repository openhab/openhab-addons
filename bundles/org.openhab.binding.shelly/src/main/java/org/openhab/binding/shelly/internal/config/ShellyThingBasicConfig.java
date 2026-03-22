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

    protected String userId = ""; // userid for http basic auth
    protected String password = ""; // password for http basic auth

    protected int updateInterval = 60; // schedule interval for the update job
    protected int lowBattery = 15; // threshold for battery value
    protected boolean brightnessAutoOn = true; // true: turn on device if brightness > 0 is set

    protected int favoriteUP = 0; // Roller position favorite when control channel receives ON, 0=none
    protected int favoriteDOWN = 0; // Roller position favorite when control channel receives ON, 0=none

    // Gen1
    protected boolean eventsButton = false; // true: register for Relay btn_xxx events
    protected boolean eventsSwitch = true; // true: register for device out_xxx events
    protected boolean eventsPush = true; // true: register for short/long push events
    protected boolean eventsRoller = true; // true: register for short/long push events
    protected boolean eventsSensorReport = true; // true: register for sensor events
    protected boolean eventsCoIoT = false; // true: use CoIoT events (based on COAP)

    // Gen2
    protected Boolean enableBluGateway = false;
    protected Boolean enableRangeExtender = true;

    public synchronized String getDeviceIp() {
        String value = deviceIp;
        return value;
    }

    public synchronized String getDeviceAddress() {
        String value = deviceAddress;
        return value;
    }

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

    public synchronized int getUpdateInterval() {
        return updateInterval;
    }

    public synchronized int getLowBattery() {
        return lowBattery;
    }

    public synchronized boolean getBrightnessAutoOn() {
        return brightnessAutoOn;
    }

    public synchronized int getFavoriteUP() {
        return favoriteUP;
    }

    public synchronized int getFavoriteDOWN() {
        return favoriteDOWN;
    }

    public synchronized boolean getEnableBluGateway() {
        return enableBluGateway;
    }

    public synchronized boolean getEnableEnableRangeExtender() {
        return enableRangeExtender;
    }

    public synchronized boolean getEventsButton() {
        return eventsButton;
    }

    public synchronized boolean getEventsSwitch() {
        return eventsSwitch;
    }

    public synchronized boolean getEventsPush() {
        return eventsPush;
    }

    public synchronized boolean getEventsRoller() {
        return eventsRoller;
    }

    public synchronized boolean getEventsSensorReport() {
        return eventsSensorReport;
    }

    public synchronized boolean getEventsCoIoT() {
        return eventsCoIoT;
    }

}
