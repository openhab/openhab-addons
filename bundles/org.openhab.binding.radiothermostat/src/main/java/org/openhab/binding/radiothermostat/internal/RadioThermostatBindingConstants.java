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
package org.openhab.binding.radiothermostat.internal;

import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RadioThermostatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatBindingConstants {

    public static final String BINDING_ID = "radiothermostat";
    public static final String LOCAL = "local";
    public static final String PROPERTY_IP = "hostName";
    public static final String PROPERTY_ISCT80 = "isCT80";
    public static final String JSON_TIME = "{\"day\":%s,\"hour\":%s,\"minute\":%s}";
    public static final String JSON_PMA = "{\"line\":1,\"message\":\"%s\"}";
    public static final String BLANK = "";

    public static final String KEY_ERROR = "error";

    // List of JSON resources
    public static final String DEFAULT_RESOURCE = "tstat";
    public static final String RUNTIME_RESOURCE = "tstat/datalog";
    public static final String HUMIDITY_RESOURCE = "tstat/humidity";
    public static final String REMOTE_TEMP_RESOURCE = "tstat/remote_temp";
    public static final String TIME_RESOURCE = "tstat/time";
    public static final String HEAT_PROGRAM_RESOURCE = "tstat/program/heat";
    public static final String COOL_PROGRAM_RESOURCE = "tstat/program/cool";
    public static final String PMA_RESOURCE = "tstat/pma";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RTHERM = new ThingTypeUID(BINDING_ID, "rtherm");

    // List of all Channel id's
    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String MODE = "mode";
    public static final String FAN_MODE = "fan_mode";
    public static final String PROGRAM_MODE = "program_mode";
    public static final String SET_POINT = "set_point";
    public static final String OVERRIDE = "override";
    public static final String HOLD = "hold";
    public static final String STATUS = "status";
    public static final String FAN_STATUS = "fan_status";
    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String DATE_STAMP = "dt_stamp";
    public static final String TODAY_HEAT_RUNTIME = "today_heat_runtime";
    public static final String TODAY_COOL_RUNTIME = "today_cool_runtime";
    public static final String YESTERDAY_HEAT_RUNTIME = "yesterday_heat_runtime";
    public static final String YESTERDAY_COOL_RUNTIME = "yesterday_cool_runtime";
    public static final String REMOTE_TEMP = "remote_temp";
    public static final String MESSAGE = "message";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_RTHERM);

    public static final Set<String> SUPPORTED_CHANNEL_IDS = Set.of(TEMPERATURE, HUMIDITY, MODE, FAN_MODE, PROGRAM_MODE,
            SET_POINT, OVERRIDE, HOLD, STATUS, FAN_STATUS, DAY, HOUR, MINUTE, DATE_STAMP, TODAY_HEAT_RUNTIME,
            TODAY_COOL_RUNTIME, YESTERDAY_HEAT_RUNTIME, YESTERDAY_COOL_RUNTIME, REMOTE_TEMP, MESSAGE);

    public static final Set<String> NO_UPDATE_CHANNEL_IDS = Set.of(REMOTE_TEMP, MESSAGE);

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = ImperialUnits.FAHRENHEIT;
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Time> API_MINUTES_UNIT = Units.MINUTE;
}
