/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AirQualityBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Łukasz Dywicki
 */
public class AirQualityBindingConstants {

    public static final String BINDING_ID = "airquality";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AQI = new ThingTypeUID(BINDING_ID, "aqi");

    // List of all Channel id's
    public static final String AQI = "aqiLevel";
    public static final String AQIDESCRIPTION = "aqiDescription";
    public static final String PM25 = "pm25";
    public static final String PM10 = "pm10";
    public static final String O3 = "o3";
    public static final String NO2 = "no2";
    public static final String CO = "co";
    public static final String LOCATIONNAME = "locationName";
    public static final String STATIONLOCATION = "stationLocation";
    public static final String STATIONID = "stationId";
    public static final String OBSERVATIONTIME = "observationTime";
    public static final String TEMPERATURE = "temperature";
    public static final String PRESSURE = "pressure";
    public static final String HUMIDITY = "humidity";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_AQI);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(AQI, AQIDESCRIPTION, PM25, PM10, O3, NO2,
            CO, LOCATIONNAME, STATIONLOCATION, STATIONID, OBSERVATIONTIME, TEMPERATURE, PRESSURE, HUMIDITY);

}
