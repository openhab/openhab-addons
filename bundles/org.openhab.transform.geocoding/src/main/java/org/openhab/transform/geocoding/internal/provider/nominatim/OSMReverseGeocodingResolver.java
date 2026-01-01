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
package org.openhab.transform.geocoding.internal.provider.nominatim;

import static org.openhab.transform.geocoding.internal.GeoProfileConstants.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.GeocodingResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OSMReverseGeocodingResolver} is the reverse geocding resolver for Nomination / OpenStreetMap API. Given
 * geo coordinates will be resolved into a human readable address string.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OSMReverseGeocodingResolver extends GeocodingResolver {
    private final Logger logger = LoggerFactory.getLogger(OSMReverseGeocodingResolver.class);

    private @Nullable PointType location;

    public OSMReverseGeocodingResolver(State state, GeoProfileConfig config, HttpClient httpClient) {
        super(state, config, httpClient);
        if (state instanceof PointType pointType) {
            location = pointType;
        }
    }

    /**
     * Performs the reverse geocoding HTTP request
     *
     * @param point the PointType containing latitude and longitude
     */
    @Override
    public void resolve() {
        PointType localLocation = location;
        if (localLocation == null) {
            logger.info("State {} isn't a location and cannot be reolved into an address", toBeResolved.toFullString());
            return;
        }
        if (isResolved()) {
            logger.trace("Location {} is already resolved {}", toBeResolved.toFullString(), getResolved());
            return;
        }

        // after check for right state and resolve status do the actual reolve functionality
        try {
            ContentResponse response = httpClient
                    .newRequest(String.format(Locale.US, REVERSE_URL, localLocation.getLatitude().doubleValue(),
                            localLocation.getLongitude().doubleValue()))
                    .header(HttpHeader.ACCEPT_LANGUAGE, config.language)
                    .header(HttpHeader.USER_AGENT, userAgentSupplier.get())
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                resolvedString = format(jsonResponse);
            } else {
                logger.debug("Decoding of location {} failed with status {} and response: {}",
                        localLocation.toFullString(), statusResponse, jsonResponse);
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Decoding of location {} failed with exception {}", localLocation.toFullString(),
                    e.getMessage());
        } catch (InterruptedException ie) {
            logger.debug("Decoding of location {} interrupted {}", localLocation.toFullString(), ie.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return toBeResolved.toFullString();
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

    @SafeVarargs
    private final String decodeAddress(JSONObject jsonObject, List<String> streetPart1, List<String> streetPart2,
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
