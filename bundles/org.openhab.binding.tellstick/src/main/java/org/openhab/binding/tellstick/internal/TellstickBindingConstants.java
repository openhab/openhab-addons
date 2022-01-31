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
package org.openhab.binding.tellstick.internal;

import static org.openhab.core.library.unit.MetricPrefix.*;

import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TellstickBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author jarlebh - Initial contribution
 */
@NonNullByDefault
public class TellstickBindingConstants {

    public static final String BINDING_ID = "tellstick";

    public static final Unit<Dimensionless> HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Pressure> PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Speed> WIND_SPEED_UNIT_MS = Units.METRE_PER_SECOND;
    public static final Unit<Angle> WIND_DIRECTION_UNIT = Units.DEGREE_ANGLE;
    public static final Unit<Length> RAIN_UNIT = MILLI(SIUnits.METRE);
    public static final Unit<Illuminance> LUX_UNIT = Units.LUX;
    public static final Unit<ElectricCurrent> ELECTRIC_UNIT = Units.AMPERE;
    public static final Unit<Power> POWER_UNIT = KILO(Units.WATT);

    public static final String CONFIGPATH_ID = "location";
    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_PROTOCOL = "protocol";
    public static final String DEVICE_MODEL = "model";
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_RESEND_COUNT = "repeat";
    public static final String DEVICE_ISDIMMER = "dimmer";
    public static final String BRIDGE_TELLDUS_CORE = "telldus-core";
    public static final String BRIDGE_TELLDUS_LIVE = "telldus-live";
    public static final String BRIDGE_TELLDUS_LOCAL = "telldus-local";
    public static final String DEVICE_SENSOR = "sensor";
    public static final String DEVICE_WINDSENSOR = "windsensor";
    public static final String DEVICE_RAINSENSOR = "rainsensor";
    public static final String DEVICE_POWERSENSOR = "powersensor";
    public static final String DEVICE_DIMMER = "dimmer";
    public static final String DEVICE_SWITCH = "switch";
    // List of all Thing Type UIDs
    public static final ThingTypeUID DIMMER_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DIMMER);
    public static final ThingTypeUID SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SWITCH);
    public static final ThingTypeUID SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SENSOR);
    public static final ThingTypeUID RAINSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_RAINSENSOR);
    public static final ThingTypeUID POWERSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_POWERSENSOR);
    public static final ThingTypeUID WINDSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_WINDSENSOR);

    public static final ThingTypeUID TELLDUSBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_CORE);
    public static final ThingTypeUID TELLDUSCOREBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_CORE);
    public static final ThingTypeUID TELLDUSLIVEBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_LIVE);
    public static final ThingTypeUID TELLDUSLOCALBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_LOCAL);
    // List of all Channel ids
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_RAINTOTAL = "raintotal";
    public static final String CHANNEL_RAINRATE = "rainrate";
    public static final String CHANNEL_WINDAVERAGE = "windaverage";
    public static final String CHANNEL_WINDDIRECTION = "winddirection";
    public static final String CHANNEL_WINDGUST = "windgust";
    public static final String CHANNEL_WATT = "watt";
    public static final String CHANNEL_AMPERE = "ampere";
    public static final String CHANNEL_LUX = "lux";

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Set.of(TELLDUSCOREBRIDGE_THING_TYPE,
            TELLDUSLIVEBRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(DIMMER_THING_TYPE,
            SWITCH_THING_TYPE, SENSOR_THING_TYPE, RAINSENSOR_THING_TYPE, WINDSENSOR_THING_TYPE, POWERSENSOR_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(DIMMER_THING_TYPE, SWITCH_THING_TYPE,
            SENSOR_THING_TYPE, RAINSENSOR_THING_TYPE, WINDSENSOR_THING_TYPE, POWERSENSOR_THING_TYPE,
            TELLDUSCOREBRIDGE_THING_TYPE, TELLDUSLIVEBRIDGE_THING_TYPE, TELLDUSLOCALBRIDGE_THING_TYPE);
}
