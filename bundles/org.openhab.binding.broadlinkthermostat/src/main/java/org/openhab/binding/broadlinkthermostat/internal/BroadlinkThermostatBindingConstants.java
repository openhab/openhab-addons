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
package org.openhab.binding.broadlinkthermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BroadlinkThermostatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class BroadlinkThermostatBindingConstants {

    private static final String BINDING_ID = "broadlinkthermostat";

    // List of all Thing Type UIDs
    public static final ThingTypeUID FLOUREON_THERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID,
            "floureonthermostat");
    public static final ThingTypeUID HYSEN_THERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, "hysenthermostat");
    public static final ThingTypeUID UNKNOWN_BROADLINKTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID,
            "unknownbroadlinkthermostatdevice");

    // List of all Channel ids
    public static final String ROOM_TEMPERATURE = "roomtemperature";
    public static final String ROOM_TEMPERATURE_EXTERNAL_SENSOR = "roomtemperatureexternalsensor";
    public static final String SETPOINT = "setpoint";
    public static final String POWER = "power";
    public static final String MODE = "mode";
    public static final String SENSOR = "sensor";
    public static final String TEMPERATURE_OFFSET = "temperatureoffset";
    public static final String ACTIVE = "active";
    public static final String REMOTE_LOCK = "remotelock";
    public static final String TIME = "time";

    // Config properties
    public static final String HOST = "host";
    public static final String DESCRIPTION = "description";

    public static final String MODE_AUTO = "auto";
    public static final String SENSOR_INTERNAL = "internal";
    public static final String SENSOR_EXTERNAL = "external";
}
