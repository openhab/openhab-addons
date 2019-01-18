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
package org.openhab.binding.synopanalyzer.internal;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.HECTO;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SynopAnalyzerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SynopAnalyzerBindingConstants {

    public static final String BINDING_ID = "synopanalyzer";

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
    public static final Unit<Speed> WIND_SPEED_UNIT_MS = SIUnits.METRE_PER_SECOND;
    public static final Unit<Speed> WIND_SPEED_UNIT_KNOT = SmartHomeUnits.KNOT;
    public static final Unit<Angle> WIND_DIRECTION_UNIT = SmartHomeUnits.DEGREE_ANGLE;
    public static final String[] WIND_DIRECTIONS = new String[] { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S",
            "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
}
