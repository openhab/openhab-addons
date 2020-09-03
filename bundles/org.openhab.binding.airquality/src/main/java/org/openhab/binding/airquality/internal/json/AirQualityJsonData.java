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
package org.openhab.binding.airquality.internal.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AirQualityJsonData} is responsible for storing
 * the "data" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityJsonData {

    private int aqi;
    private int idx;

    private @NonNullByDefault({}) AirQualityJsonTime time;
    private @NonNullByDefault({}) AirQualityJsonCity city;
    private List<Attribute> attributions = new ArrayList<>();
    private Map<String, @Nullable AirQualityValue> iaqi = new HashMap<>();
    private String dominentpol = "";

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
        List<String> list = new ArrayList<>();
        attributions.forEach(item -> list.add(item.getName()));
        return "Attributions : " + String.join(", ", list);
    }

    public String getDominentPol() {
        return dominentpol;
    }

    public double getIaqiValue(String key) {
        AirQualityValue result = iaqi.get(key);
        if (result != null) {
            return result.getValue();
        }
        return -1;
    }
}
