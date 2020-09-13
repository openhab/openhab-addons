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
package org.openhab.binding.airquality.internal;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.HECTO;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link AirQualityBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Łukasz Dywicki - Initial contribution
 */
@NonNullByDefault
public class AirQualityBindingConstants {

    public static final String BINDING_ID = "airquality";
    public static final String LOCAL = "local";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AQI = new ThingTypeUID(BINDING_ID, "aqi");

    // List of all Channel id's
    public static final String AQI = "aqiLevel";
    public static final String AQI_COLOR = "aqiColor";
    public static final String AQIDESCRIPTION = "aqiDescription";
    public static final String PM25 = "pm25";
    public static final String PM10 = "pm10";
    public static final String O3 = "o3";
    public static final String NO2 = "no2";
    public static final String CO = "co";
    public static final String SO2 = "so2";
    public static final String LOCATIONNAME = "locationName";
    public static final String STATIONLOCATION = "stationLocation";
    public static final String STATIONID = "stationId";
    public static final String OBSERVATIONTIME = "observationTime";
    public static final String TEMPERATURE = "temperature";
    public static final String PRESSURE = "pressure";
    public static final String HUMIDITY = "humidity";
    public static final String DOMINENTPOL = "dominentpol";

    public static final State GOOD = new StringType("GOOD");
    public static final State MODERATE = new StringType("MODERATE");
    public static final State UNHEALTHY_FOR_SENSITIVE = new StringType("UNHEALTHY_FOR_SENSITIVE");
    public static final State UNHEALTHY = new StringType("UNHEALTHY");
    public static final State VERY_UNHEALTHY = new StringType("VERY_UNHEALTHY");
    public static final State HAZARDOUS = new StringType("HAZARDOUS");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AQI);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream.of(AQI, AQIDESCRIPTION, PM25, PM10, O3, NO2, CO, SO2,
            LOCATIONNAME, STATIONLOCATION, STATIONID, OBSERVATIONTIME, TEMPERATURE, PRESSURE, HUMIDITY)
            .collect(Collectors.toSet());

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> API_HUMIDITY_UNIT = SmartHomeUnits.PERCENT;
    public static final Unit<Pressure> API_PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
}
