/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.api.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.api.Pollutant;

/**
 * The {@link AirQualityData} is responsible for storing
 * the "data" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityData extends ResponseRoot {

    private int aqi;
    private int idx;

    private @Nullable AirQualityTime time;
    private @Nullable AirQualityCity city;
    private List<Attribution> attributions = List.of();
    private Map<String, AirQualityValue> iaqi = Map.of();
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
    public Optional<AirQualityTime> getTime() {
        return Optional.ofNullable(time);
    }

    /**
     * Receives "city" node from the "data" object in JSON response
     *
     * @return {AirQualityJsonCity}
     */
    public Optional<AirQualityCity> getCity() {
        return Optional.ofNullable(city);
    }

    /**
     * Collects a list of attributions (vendors making data available)
     * and transforms it into readable string.
     *
     * @return {String}
     */
    public String getAttributions() {
        return attributions.stream().map(Attribution::getName).collect(Collectors.joining(", "));
    }

    public String getDominentPol() {
        return dominentpol;
    }

    public double getIaqiValue(String key) {
        AirQualityValue result = iaqi.get(key);
        return result != null ? result.getValue() : -1;
    }

    public double getIaqiValue(Pollutant pollutant) {
        return getIaqiValue(pollutant.name().toLowerCase());
    }
}
