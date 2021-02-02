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
package org.openhab.binding.novelanheatpump.internal;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NovelanHeatpumpBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 */
@NonNullByDefault
public class NovelanHeatpumpBindingConstants {

    private static final String BINDING_ID = "novelanheatpump";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heatpump");

    // List of all Channel ids
    public static final String TEMPERATURE_OUTSIDE = "temperatureOutside";
    public static final String TEMPERATURE_OUTSIDE_AVG = "temperatureOutsideAvg";
    public static final String TEMPERATURE_RETURN = "temperatureReturn";
    public static final String TEMPERATURE_REFERENCE_RETURN = "temperatureReferenceReturn";
    public static final String TEMPERATURE_SUPPLAY = "temperatureSupplay";
    public static final String TEMPERATURE_SERVICEWATER_REFERENCE = "temperatureServicewaterReference";
    public static final String TEMPERATURE_SERVICEWATER = "temperatureServicewater";
    public static final String STATE_DURATION = "stateDuration";
    public static final String SIMPLE_STATE = "simpleState";
    public static final String SIMPLE_STATE_NUM = "simpleStateNum";
    public static final String EXTENDED_STATE = "extendedState";
    public static final String TEMPERATURE_SOLAR_COLLECTOR = "temperatureSolarCollector";
    public static final String TEMPERATURE_PROBE_IN = "temperatureProbeIn";
    public static final String TEMPERATURE_PROBE_OUT = "temperatureProbeOut";
    public static final String HOURS_COMPRESSOR1 = "hoursCompressor1";
    public static final String STARTS_COMPRESSOR1 = "startsCompressor1";
    public static final String HOURS_COMPRESSOR2 = "hoursCompressor2";
    public static final String STARTS_COMPRESSOR2 = "startsCompressor2";
    public static final String HOURS_HEATPUMP = "hoursHeatpump";
    public static final String HOURS_HEATING = "hoursHeating";
    public static final String HOURS_WARMWATER = "hoursWarmwater";
    public static final String HOURS_COOLING = "hoursCooling";
    public static final String THERMALENERGY_HEATING = "thermalenergyHeating";
    public static final String THERMALENERGY_WARMWATER = "thermalenergyWarmwater";
    public static final String THERMALENERGY_POOL = "thermalenergyPool";
    public static final String THERMALENERGY_TOTAL = "thermalenergyTotal";

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Energy> API_POWER_UNIT = Units.KILOWATT_HOUR;
}
