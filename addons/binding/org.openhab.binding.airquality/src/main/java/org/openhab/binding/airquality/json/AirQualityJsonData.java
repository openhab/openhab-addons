/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link AirQualityJsonData} is responsible for storing
 * the "data" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityJsonData {

    private int aqi;
    private int idx;

    private static final String GOOD = "GOOD";
    private static final String MODERATE = "MODERATE";
    private static final String UNHEALTHY_FOR_SENSITIVE = "UNHEALTHY_FOR_SENSITIVE";
    private static final String UNHEALTHY = "UNHEALTHY";
    private static final String VERY_UNHEALTHY = "VERY_UNHEALTHY";
    private static final String HAZARDOUS = "HAZARDOUS";
    private static final String NO_DATA = "NO_DATA";

    private AirQualityJsonTime time;
    private AirQualityJsonCity city;
    private List<Attribute> attributions;
    private AirQualityJsonIaqi iaqi;

    public AirQualityJsonData() {
    }

    /**
     * Air Quality Index
     *
     * @return {Integer}
     */
    public int getAqi() {
        return aqi;
    }

    /**
     * Measuring Station ID
     *
     * @return {Integer}
     */
    public int getStationId() {
        return idx;
    }

    /**
     * Receives "time" node from the "data" object in JSON response
     *
     * @return {AirQualityJsonTime}
     */
    public AirQualityJsonTime getTime() {
        return time;
    }

    /**
     * Receives "city" node from the "data" object in JSON response
     *
     * @return {AirQualityJsonCity}
     */
    public AirQualityJsonCity getCity() {
        return city;
    }

    /**
     * Collects a list of attributions (vendors making data available)
     * and transforms it into readable string.
     * Currently displayed in Thing Status description when ONLINE
     *
     * @return {String}
     */
    public String getAttributions() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < attributions.size(); i++) {
            list.add(attributions.get(i).getName());
        }
        return "Attributions: " + String.join(", ", list);
    }

    /**
     * Receives "iaqi" node from the "data" object in JSON response
     *
     * @return {AirQualityJsonIaqi}
     */
    public AirQualityJsonIaqi getIaqi() {
        return iaqi;
    }

    /**
     * Interprets the current aqi value within the ranges;
     * Returns AQI in a human readable format
     *
     * @return
     */
    public String getAqiDescription() {
        if (aqi > 0 && aqi <= 50) {
            return GOOD;
        } else if (aqi >= 51 && aqi <= 100) {
            return MODERATE;
        } else if (aqi >= 101 && aqi <= 150) {
            return UNHEALTHY_FOR_SENSITIVE;
        } else if (aqi >= 151 && aqi <= 200) {
            return UNHEALTHY;
        } else if (aqi >= 201 && aqi < 300) {
            return VERY_UNHEALTHY;
        } else if (aqi >= 300) {
            return HAZARDOUS;
        }

        return NO_DATA;
    }

}
