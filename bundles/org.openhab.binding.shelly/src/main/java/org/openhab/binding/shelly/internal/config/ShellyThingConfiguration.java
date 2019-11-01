/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * @author Hans-Jörg - Initial contribution
 * @author Markus Michels - refactored
 */
@NonNullByDefault
public class ShellyThingConfiguration {
    public String localIp = ""; // local ip addresses used to create callback url

    public String deviceIp = ""; // ip address of thedevice
    public int updateInterval = 60; // schedule interval for the update job
    public float lowBattery = 20; // threshold for battery value

    public String userId = ""; // userid for http basic auth
    public String password = ""; // password for http basic auth

    public boolean eventsButton = false; // true: register for Relay btn_xxx events
    public boolean eventsSwitch = true; // true: register for de vice out_xxx events
    public boolean eventsSensorReport = true; // true: register for sensor events
    public boolean eventsCoIoT = false; // true: use CoIoT events (based on COAP)
}
