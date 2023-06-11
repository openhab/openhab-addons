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
package org.openhab.binding.ambientweather.internal.model;

/**
 * The {@link DeviceJson} is the JSON object returned
 * by the Ambient Weather '/v1/devices' command.
 * Note that there is nothing in the JSON that defines the type (e.g.
 * WS-1400-IP, WS-8482, etc.) of weather station.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class DeviceJson {
    /*
     * The weather station's MAC address
     */
    public String macAddress;

    /*
     * API key to which this device is associated
     */
    public String apiKey;

    /*
     * User-supplied information about the station, such as name and location
     */
    public StationInfoJson info;

    /*
     * Placeholder for weather data, which varies by station.
     */
    public Object lastData;
}
