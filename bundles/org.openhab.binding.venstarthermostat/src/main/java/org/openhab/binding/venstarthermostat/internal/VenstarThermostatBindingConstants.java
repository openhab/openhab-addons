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
package org.openhab.binding.venstarthermostat.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VenstarThermostatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author William Welliver - Initial contribution
 * @author Matthew Davies - added awayMode and awayModeRaw to include thermostat away mode in binding
 * @author Matthew Davies - added more binding functionality to get close to the API functionality
 */
@NonNullByDefault
public class VenstarThermostatBindingConstants {

    public static final String BINDING_ID = "venstarthermostat";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_COLOR_TOUCH = new ThingTypeUID(BINDING_ID, "colorTouchThermostat");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_COLOR_TOUCH);
    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_EXTERNAL_TEMPERATURE = "outdoorTemperature";

    public static final String CHANNEL_HEATING_SETPOINT = "heatingSetpoint";
    public static final String CHANNEL_COOLING_SETPOINT = "coolingSetpoint";
    public static final String CHANNEL_SYSTEM_STATE = "systemState";
    public static final String CHANNEL_SYSTEM_MODE = "systemMode";
    public static final String CHANNEL_SYSTEM_STATE_RAW = "systemStateRaw";
    public static final String CHANNEL_SYSTEM_MODE_RAW = "systemModeRaw";
    public static final String CHANNEL_AWAY_MODE = "awayMode";
    public static final String CHANNEL_AWAY_MODE_RAW = "awayModeRaw";
    public static final String CHANNEL_FAN_MODE = "fanMode";
    public static final String CHANNEL_FAN_MODE_RAW = "fanModeRaw";
    public static final String CHANNEL_FAN_STATE = "fanState";
    public static final String CHANNEL_FAN_STATE_RAW = "fanStateRaw";
    public static final String CHANNEL_SCHEDULE_MODE = "scheduleMode";
    public static final String CHANNEL_SCHEDULE_MODE_RAW = "scheduleModeRaw";
    public static final String CHANNEL_SCHEDULE_PART = "schedulePart";
    public static final String CHANNEL_SCHEDULE_PART_RAW = "schedulePartRaw";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY = "timestampDay";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY = "heat1RuntimeDay";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY = "heat2RuntimeDay";
    public static final String CHANNEL_COOL1_RUNTIME_DAY = "cool1RuntimeDay";
    public static final String CHANNEL_COOL2_RUNTIME_DAY = "cool2RuntimeDay";
    public static final String CHANNEL_AUX1_RUNTIME_DAY = "aux1RuntimeDay";
    public static final String CHANNEL_AUX2_RUNTIME_DAY = "aux2RuntimeDay";
    public static final String CHANNEL_FC_RUNTIME_DAY = "freeCoolRuntimeDay";
    // add query/runtimes and query/alerts - these will need an additional class similar to Venstar.infodata - more work

    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH = "refresh";

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_UUID = "uuid";

    public static final String REFRESH_INVALID = "refresh-invalid";
    public static final String EMPTY_INVALID = "empty-invalid";
}
