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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensorState} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link SensorMessage}.
 *
 * This should be in sync with the supported sensors from
 * https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorState {
    /** Some presence sensors, the daylight sensor and all light sensors provide the "dark" boolean. */
    public @Nullable Boolean dark;
    /** The daylight sensor and all light sensors provides the "daylight" boolean. */
    public @Nullable Boolean daylight;
    /** Light sensors provide a light level value. */
    public @Nullable Integer lightlevel;
    /** Light sensors provide a lux value. */
    public @Nullable Integer lux;
    /** Temperature sensors provide a degrees value. */
    public @Nullable Float temperature;
    /** Humidity sensors provide a percent value. */
    public @Nullable Float humidity;
    /** OpenClose sensors provide a boolean value. */
    public @Nullable Boolean open;
    /** fire sensors provide a boolean value. */
    public @Nullable Boolean fire;
    /** water sensors provide a boolean value. */
    public @Nullable Boolean water;
    /** vibration sensors provide a boolean value. */
    public @Nullable Boolean vibration;
    /** carbonmonoxide sensors provide a boolean value. */
    public @Nullable Boolean carbonmonoxide;
    /** Pressure sensors provide a hPa value. */
    public @Nullable Integer pressure;
    /** Presence sensors provide this boolean. */
    public @Nullable Boolean presence;
    /** Power sensors provide this value in Watts. */
    public @Nullable Float power;
    /** Consumption sensors provide this value in Watts/hour. */
    public @Nullable Float consumption;
    /** Power sensors provide this value in Volt. */
    public @Nullable Float voltage;
    /** Power sensors provide this value in Milliampere. */
    public @Nullable Float current;
    /** Light sensors and the daylight sensor provide a status integer that can have various semantics. */
    public @Nullable Integer status;
    /** Switches provide this value. */
    public @Nullable Integer buttonevent;
    /** deCONZ sends a last update string with every event. */
    public @Nullable String lastupdated;
}
