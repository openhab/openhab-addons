/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LuftdatenInfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LuftdatenInfoBindingConstants {

    private static final String BINDING_ID = "luftdateninfo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PARTICULATE = new ThingTypeUID(BINDING_ID, "particulate");
    public static final ThingTypeUID THING_TYPE_CONDITIONS = new ThingTypeUID(BINDING_ID, "conditions");
    public static final ThingTypeUID THING_TYPE_NOISE = new ThingTypeUID(BINDING_ID, "noise");

    // List of all Channel ids
    public static final String PM25_CHANNEL = "pm25";
    public static final String PM100_CHANNEL = "pm100";
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String PRESSURE_CHANNEL = "pressure";
    public static final String PRESSURE_SEA_CHANNEL = "pressure-sea";
    public static final String NOISE_EQ_CHANNEL = "noise-eq";
    public static final String NOISE_MIN_CHANNEL = "noise-min";
    public static final String NOISE_MAX_CHANNEL = "noise-max";
}
