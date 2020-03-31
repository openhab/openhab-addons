/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RadioThermostatBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatBindingConstants {

    public static final String BINDING_ID = "radiothermostat";
    public static final String LOCAL = "local";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RTHERM = new ThingTypeUID(BINDING_ID, "rtherm");
    
    // List of all Channel id's
    public static final String NAME = "name";
    public static final String MODEL = "model";
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
    public static final String LAST_UPDATE ="last_update";
    public static final String TODAY_HEAT_HOUR ="today_heat_hour";
    public static final String TODAY_HEAT_MINUTE ="today_heat_minute";
    public static final String TODAY_COOL_HOUR ="today_cool_hour";
    public static final String TODAY_COOL_MINUTE ="today_cool_minute";
    public static final String YESTERDAY_HEAT_HOUR ="yesterday_heat_hour";
    public static final String YESTERDAY_HEAT_MINUTE ="yesterday_heat_minute";
    public static final String YESTERDAY_COOL_HOUR ="yesterday_cool_hour";
    public static final String YESTERDAY_COOL_MINUTE ="yesterday_cool_minute";
    
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_RTHERM);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream.of(NAME, MODEL, TEMPERATURE, HUMIDITY, MODE, FAN_MODE, PROGRAM_MODE,
            SET_POINT, OVERRIDE, HOLD, STATUS, FAN_STATUS, DAY, HOUR, MINUTE, DATE_STAMP, LAST_UPDATE, TODAY_HEAT_HOUR, TODAY_HEAT_MINUTE,
            TODAY_COOL_HOUR, TODAY_COOL_MINUTE, YESTERDAY_HEAT_HOUR, YESTERDAY_HEAT_MINUTE, YESTERDAY_COOL_HOUR, YESTERDAY_COOL_MINUTE)
            .collect(Collectors.toSet());

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = ImperialUnits.FAHRENHEIT;
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = SmartHomeUnits.PERCENT;


}
