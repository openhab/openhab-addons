/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VenstarThermostatBinding} class defines common constants, which are
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COLOR_TOUCH);
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
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY0 = "timestampDay0";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY1 = "timestampDay1";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY2 = "timestampDay2";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY3 = "timestampDay3";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY4 = "timestampDay4";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY5 = "timestampDay5";
    public static final String CHANNEL_TIMESTAMP_RUNTIME_DAY6 = "timestampDay6";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY0 = "heat1RuntimeDay0";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY1 = "heat1RuntimeDay1";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY2 = "heat1RuntimeDay2";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY3 = "heat1RuntimeDay3";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY4 = "heat1RuntimeDay4";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY5 = "heat1RuntimeDay5";
    public static final String CHANNEL_HEAT1_RUNTIME_DAY6 = "heat1RuntimeDay6";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY0 = "heat2RuntimeDay0";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY1 = "heat2RuntimeDay1";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY2 = "heat2RuntimeDay2";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY3 = "heat2RuntimeDay3";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY4 = "heat2RuntimeDay4";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY5 = "heat2RuntimeDay5";
    public static final String CHANNEL_HEAT2_RUNTIME_DAY6 = "heat2RuntimeDay6";
    public static final String CHANNEL_COOL1_RUNTIME_DAY0 = "cool1RuntimeDay0";
    public static final String CHANNEL_COOL1_RUNTIME_DAY1 = "cool1RuntimeDay1";
    public static final String CHANNEL_COOL1_RUNTIME_DAY2 = "cool1RuntimeDay2";
    public static final String CHANNEL_COOL1_RUNTIME_DAY3 = "cool1RuntimeDay3";
    public static final String CHANNEL_COOL1_RUNTIME_DAY4 = "cool1RuntimeDay4";
    public static final String CHANNEL_COOL1_RUNTIME_DAY5 = "cool1RuntimeDay5";
    public static final String CHANNEL_COOL1_RUNTIME_DAY6 = "cool1RuntimeDay6";
    public static final String CHANNEL_COOL2_RUNTIME_DAY0 = "cool2RuntimeDay0";
    public static final String CHANNEL_COOL2_RUNTIME_DAY1 = "cool2RuntimeDay1";
    public static final String CHANNEL_COOL2_RUNTIME_DAY2 = "cool2RuntimeDay2";
    public static final String CHANNEL_COOL2_RUNTIME_DAY3 = "cool2RuntimeDay3";
    public static final String CHANNEL_COOL2_RUNTIME_DAY4 = "cool2RuntimeDay4";
    public static final String CHANNEL_COOL2_RUNTIME_DAY5 = "cool2RuntimeDay5";
    public static final String CHANNEL_COOL2_RUNTIME_DAY6 = "cool2RuntimeDay6";
    public static final String CHANNEL_AUX1_RUNTIME_DAY0 = "aux1RuntimeDay0";
    public static final String CHANNEL_AUX1_RUNTIME_DAY1 = "aux1RuntimeDay1";
    public static final String CHANNEL_AUX1_RUNTIME_DAY2 = "aux1RuntimeDay2";
    public static final String CHANNEL_AUX1_RUNTIME_DAY3 = "aux1RuntimeDay3";
    public static final String CHANNEL_AUX1_RUNTIME_DAY4 = "aux1RuntimeDay4";
    public static final String CHANNEL_AUX1_RUNTIME_DAY5 = "aux1RuntimeDay5";
    public static final String CHANNEL_AUX1_RUNTIME_DAY6 = "aux1RuntimeDay6";
    public static final String CHANNEL_AUX2_RUNTIME_DAY0 = "aux2RuntimeDay0";
    public static final String CHANNEL_AUX2_RUNTIME_DAY1 = "aux2RuntimeDay1";
    public static final String CHANNEL_AUX2_RUNTIME_DAY2 = "aux2RuntimeDay2";
    public static final String CHANNEL_AUX2_RUNTIME_DAY3 = "aux2RuntimeDay3";
    public static final String CHANNEL_AUX2_RUNTIME_DAY4 = "aux2RuntimeDay4";
    public static final String CHANNEL_AUX2_RUNTIME_DAY5 = "aux2RuntimeDay5";
    public static final String CHANNEL_AUX2_RUNTIME_DAY6 = "aux2RuntimeDay6";
    public static final String CHANNEL_FC_RUNTIME_DAY0 = "freeCoolRuntimeDay0";
    public static final String CHANNEL_FC_RUNTIME_DAY1 = "freeCoolRuntimeDay1";
    public static final String CHANNEL_FC_RUNTIME_DAY2 = "freeCoolRuntimeDay2";
    public static final String CHANNEL_FC_RUNTIME_DAY3 = "freeCoolRuntimeDay3";
    public static final String CHANNEL_FC_RUNTIME_DAY4 = "freeCoolRuntimeDay4";
    public static final String CHANNEL_FC_RUNTIME_DAY5 = "freeCoolRuntimeDay5";
    public static final String CHANNEL_FC_RUNTIME_DAY6 = "freeCoolRuntimeDay6";

    // add query/runtimes and query/alerts - these will need an additional class similar to Venstar.infodata - more work

    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH = "refresh";

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_UUID = "uuid";

    public static final String REFRESH_INVALID = "refresh-invalid";
    public static final String EMPTY_INVALID = "empty-invalid";
}
