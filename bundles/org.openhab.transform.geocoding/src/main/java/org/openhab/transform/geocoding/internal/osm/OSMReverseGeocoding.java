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
package org.openhab.transform.geocoding.internal.osm;

import static org.openhab.transform.geocoding.internal.OSMGeoConstants.*;

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
import org.openhab.transform.geocoding.internal.config.OSMGeoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reverse geocoding of a given location using OpenStreetMap (OSM) Nominatim API service
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OSMReverseGeocoding {
    private final Logger logger = LoggerFactory.getLogger(OSMReverseGeocoding.class);

    private HttpClient httpClient;
    private OSMGeoConfig config;
    private boolean resolved = false;
    private PointType location;
    private @Nullable String address;

    public OSMReverseGeocoding(PointType location, OSMGeoConfig config, HttpClient httpClient) {
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
    public void resolve() {
        if (resolved) {
            logger.trace("Location {} is already resolved {}", location.toFullString(), getAddress());
            return;
        }
        try {
            ContentResponse response = httpClient
                    .newRequest(String.format(Locale.US, REVERSE_URL, location.getLatitude().doubleValue(),
                            location.getLongitude().doubleValue()))
                    .header("Accept-Language", config.language)
                    .header("User-Agent", "openHAB Geo Transformation Service").timeout(10, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                address = format(jsonResponse);
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

    public String format(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String resolvedAddress;
        switch (config.format) {
            case ROW_ADDRESS_FORMAT:
                resolvedAddress = decodeAddress(jsonObject, ROAD_KEYS, HOUSE_NUMBER_KEYS, ZIP_CODE_KEYS, CITY_KEYS,
                        DISTRICT_KEYS);
                return resolvedAddress.isBlank() ? decodeJson(jsonObject) : resolvedAddress;
            case US_ADDRESS_FORMAT:
                resolvedAddress = decodeAddress(jsonObject, HOUSE_NUMBER_KEYS, ROAD_KEYS, CITY_KEYS, DISTRICT_KEYS,
                        ZIP_CODE_KEYS);
                return resolvedAddress.isBlank() ? decodeJson(jsonObject) : resolvedAddress;
            case JSON_FORMAT:
                return decodeJson(jsonObject);
            case RAW_FORMAT:
                return jsonObject.toString();
            default:
                return decodeJson(jsonObject);
        }
    }

    private String decodeAddress(JSONObject jsonObject, List<String> streetPart1, List<String> streetPart2,
            List<String>... cityKeys) {
        if (jsonObject.has(ADDRESS_KEY)) {
            JSONObject address = jsonObject.getJSONObject(ADDRESS_KEY);
            String street = (get(address, streetPart1) + " " + get(address, streetPart2)).strip();
            StringBuilder fullAddress = new StringBuilder(street.isBlank() ? "" : street + ", ");
            for (List<String> keys : cityKeys) {
                fullAddress.append(get(address, keys)).append(" ");
            }
            return fullAddress.toString().strip();
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
        if (jsonObject.has(ADDRESS_KEY)) {
            return jsonObject.getJSONObject(ADDRESS_KEY).toString();
        }
        return jsonObject.toString();
    }
}
