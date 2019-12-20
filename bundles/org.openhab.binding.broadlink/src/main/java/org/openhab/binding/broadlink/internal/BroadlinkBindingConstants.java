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
package org.openhab.binding.broadlink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BroadlinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class BroadlinkBindingConstants {

    public static final String BINDING_ID = "broadlink";

    // List of all Thing Type UIDs
    public static final ThingTypeUID FLOUREON_THERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, "floureonthermostat");
    public static final ThingTypeUID HYSEN_THERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, "hysenthermostat");;
    public static final ThingTypeUID UNKNOWN_BROADLINK_THING_TYPE = new ThingTypeUID(BINDING_ID, "unknownbroadlinkdevice");
    public static final ThingTypeUID A1_ENVIRONMENTAL_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "a1environmentalsensor");
    public static final ThingTypeUID RM_MINI_THING_TYPE = new ThingTypeUID(BINDING_ID, "rmmini");

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


    public static final String AIR_QUALITY = "airquality";
    public static final String HUMIDITY = "humidity";
    public static final String LIGHT = "light";
    public static final String NOISE = "noise";
    public static final String TEMPERATURE = "temperature";

    // Config properties
    public static final String HOST = "host";
    public static final String MAC = "mac";
    public static final String DESCRIPTION = "description";

    public static final String MODE_AUTO = "auto";
    public static final String MODE_MANUAL = "manual";
    public static final String SENSOR_INTERNAL = "internal";
    public static final String SENSOR_EXTERNAL = "external";


}
