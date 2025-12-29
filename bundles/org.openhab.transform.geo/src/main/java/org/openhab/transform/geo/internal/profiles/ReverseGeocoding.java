/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.geo.internal.profiles;

import static org.openhab.transform.geo.internal.GeoConstants.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;
import org.openhab.transform.geo.internal.config.GeoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reverse Geocoding of JSON String
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ReverseGeocoding {
    private final Logger logger = LoggerFactory.getLogger(ReverseGeocoding.class);

    private HttpClient httpClient;
    private GeoConfig config;
    private boolean resolved = false;
    private PointType location;
    private @Nullable String address;

    public ReverseGeocoding(PointType location, GeoConfig config, HttpClient httpClient) {
        this.location = location;
        this.config = config;
        this.httpClient = httpClient;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getAddress() {
        String localAddress = address;
        if (localAddress != null) {
            return localAddress;
        }
        return "unknown";
    }

    /**
     * Performs the reverse geo coding HTTP request
     *
     * @param point the PointType containing latitude and longitude
     */
    public void doResolve() {
        try {
            ContentResponse response = httpClient
                    .newRequest(String.format(Locale.US, REVERSE_URL, location.getLatitude().doubleValue(),
                            location.getLongitude().doubleValue()))
                    .header("Accept-Language", config.language)
                    .header("User-Agent", "openHAB Geo Transformation Service").timeout(10, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                address = decode(jsonResponse);
                resolved = true;
            } else {
                logger.debug("Decoding of location {} failed with status {} and response: {}", location.toFullString(),
                        statusResponse, jsonResponse);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Decoding of location {} failed with exception {}", location.toFullString(), e.getMessage());
        }
    }

    @Override
    public String toString() {
        return location.toFullString();
    }

    public String decode(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        switch (config.format) {
            case ADDRESS_FORMAT:
                return decodeAddress(jsonObject);
            case JSON_FORMAT:
                return decodeJson(jsonObject);
            default:
                return decodeJson(jsonObject);
        }
    }

    /**
     * Decode address from JSON object with pattern street, housenumber, zipcode, city and district.
     * Some fields may be missing depending on the location.
     *
     * @param jsonObject to be decoded
     * @return human readable address string
     */
    private String decodeAddress(JSONObject jsonObject) {
        if (jsonObject.has(ADDRESS_FORMAT)) {
            JSONObject address = jsonObject.getJSONObject(ADDRESS_FORMAT);
            String street = (get(address, ROAD_KEYS) + " " + get(address, HOUSE_NUMBER_KEYS)).strip();
            String city = (get(address, ZIP_CODE_KEYS) + " " + get(address, CITY_KEYS) + " "
                    + get(address, DISTRICT_KEYS)).strip();
            if (!street.isBlank()) {
                street += ", ";
            }
            return street + city;
        }
        return "";
    }

    private String get(JSONObject jsonObject, List<String> keys) {
        for (String key : keys) {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            }
        }
        return "";
    }

    /**
     * Decode JSON object. If address is available return only the address part. Else return full JSON string.
     *
     * @param jsonObject to be decoded
     * @return JSON formatted string
     */
    private String decodeJson(JSONObject jsonObject) {
        if (jsonObject.has(ADDRESS_FORMAT)) {
            return jsonObject.getJSONObject(ADDRESS_FORMAT).toString();
        }
        return jsonObject.toString();
    }
}
