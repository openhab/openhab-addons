/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
    public final static ThingTypeUID THING_TYPE_AQI = new ThingTypeUID(BINDING_ID, "aqi");

    // List of all Channel id's
    public final static String AQI = "aqiLevel";
    public final static String AQIDESCRIPTION = "aqiDescription";
    public final static String PM25 = "pm25";
    public final static String PM10 = "pm10";
    public final static String O3 = "o3";
    public final static String NO2 = "no2";
    public final static String CO = "co";
    public final static String LOCATIONNAME = "locationName";
    public final static String STATIONLOCATION = "stationLocation";
    public final static String STATIONID = "stationId";
    public final static String OBSERVATIONTIME = "observationTime";
    public final static String TEMPERATURE = "temperature";
    public final static String PRESSURE = "pressure";
    public final static String HUMIDITY = "humidity";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_AQI);
    public final static Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(AQI, AQIDESCRIPTION, PM25, PM10, O3, NO2,
            CO, LOCATIONNAME, STATIONLOCATION, STATIONID, OBSERVATIONTIME, TEMPERATURE, PRESSURE, HUMIDITY);

}
