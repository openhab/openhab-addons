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
package org.openhab.binding.shelly.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellyThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingConfiguration {
    public String deviceIp = ""; // ip address of thedevice
    public String deviceAddress = ""; // IP address or MAC address for BLU devices
    public String userId = ""; // userid for http basic auth
    public String password = ""; // password for http basic auth

    public int updateInterval = 60; // schedule interval for the update job
    public int lowBattery = 15; // threshold for battery value
    public boolean brightnessAutoOn = true; // true: turn on device if brightness > 0 is set

    public int favoriteUP = 0; // Roller position favorite when control channel receives ON, 0=none
    public int favoriteDOWN = 0; // Roller position favorite when control channel receives ON, 0=none

    public boolean eventsButton = false; // true: register for Relay btn_xxx events
    public boolean eventsSwitch = true; // true: register for device out_xxx events
    public boolean eventsPush = true; // true: register for short/long push events
    public boolean eventsRoller = true; // true: register for short/long push events
    public boolean eventsSensorReport = true; // true: register for sensor events
    public boolean eventsCoIoT = false; // true: use CoIoT events (based on COAP)

    public String localIp = ""; // local ip addresses used to create callback url
    public String localPort = "8080";
    public String serviceName = "";

    public Boolean enableBluGateway = false;
}
