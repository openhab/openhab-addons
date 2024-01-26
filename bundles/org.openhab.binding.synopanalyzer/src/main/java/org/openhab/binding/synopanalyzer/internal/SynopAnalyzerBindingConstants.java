/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyzer.internal;

import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SynopAnalyzerBindingConstants} class defines common constants used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SynopAnalyzerBindingConstants {
    private static final String BINDING_ID = "synopanalyzer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_SYNOP = new ThingTypeUID(BINDING_ID, "synopanalyzer");

    // List of all Channel ids
    public static final String HORIZONTAL_VISIBILITY = "horizontal-visibility";
    public static final String OCTA = "octa";
    public static final String ATTENUATION_FACTOR = "attenuation-factor";
    public static final String OVERCAST = "overcast";
    public static final String PRESSURE = "pressure";
    public static final String TEMPERATURE = "temperature";
    public static final String WIND_ANGLE = "wind-angle";
    public static final String WIND_DIRECTION = "wind-direction";
    public static final String WIND_STRENGTH = "wind-speed";
    public static final String WIND_SPEED_BEAUFORT = "wind-speed-beaufort";
    public static final String TIME_UTC = "time-utc";

    // Default units
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Pressure> PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Angle> WIND_DIRECTION_UNIT = Units.DEGREE_ANGLE;

    // Synop message origin station codes
    public static final String LAND_STATION_CODE = "AAXX";
    public static final String SHIP_STATION_CODE = "BBXX";
    public static final String MOBILE_LAND_STATION_CODE = "OOXX";
}
