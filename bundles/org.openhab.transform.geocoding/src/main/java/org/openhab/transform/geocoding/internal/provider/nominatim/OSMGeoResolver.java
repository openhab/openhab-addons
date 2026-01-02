/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.BaseGeoResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OSMGeoResolver} is the reverse geocding resolver for Nomination / OpenStreetMap API. Given
 * geo coordinates will be resolved into a human readable address string.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OSMGeoResolver extends BaseGeoResolver {
    private final Logger logger = LoggerFactory.getLogger(OSMGeoResolver.class);

    public OSMGeoResolver(State state, GeoProfileConfig config, HttpClient httpClient) {
        super(state, config, httpClient);
    }

    /**
     * Execute resolving. If any error occurs (API failure, exception, ...) corresponding trace is logged the
     * resolvedString will remain null and isResolved() will return false.
     */
    @Override
    public void resolve() {
        if (isResolved()) {
            logger.trace("State {} is already resolved {}", toBeResolved.toFullString(), getResolved());
            return;
        }

        PointType localGeoLocation = geoLocation;
        String localGeoSearchString = geoSearchString;
        if (localGeoLocation != null) {
            String jsonResponse = apiCall(String.format(Locale.US, REVERSE_URL,
                    localGeoLocation.getLatitude().doubleValue(), localGeoLocation.getLongitude().doubleValue()));
            resolvedString = formatAddress(jsonResponse);
        } else if (localGeoSearchString != null) {
            try {
                String encodedSearch = URLEncoder.encode(localGeoSearchString, StandardCharsets.UTF_8.toString());
                String jsonResponse = apiCall(String.format(SEARCH_URL, encodedSearch));
                resolvedString = getGeoCoordinates(jsonResponse);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Exception during decoding of address {}: {}", localGeoSearchString, e.getMessage());
            }
        }
    }

    @Override
    public @Nullable String geoSearch(String address) {
        try {
            String encodedSearch = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String jsonResponse = apiCall(String.format(SEARCH_URL, encodedSearch));
            return getGeoCoordinates(jsonResponse);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Exception during encoding of address {}: {}", address, e.getMessage());
        }
        return null;
    }

    @Override
    public @Nullable String geoReverseSearch(PointType coordinates) {
        String jsonResponse = apiCall(String.format(Locale.US, REVERSE_URL, coordinates.getLatitude().doubleValue(),
                coordinates.getLongitude().doubleValue()));
        return formatAddress(jsonResponse);
    }

    /**
     * Execute the API call to the geocoding provider and return the JSON response as String.
     *
     * @param url for querying the geocoding provider
     */
    private String apiCall(String url) {
        try {
            ContentResponse response = httpClient.newRequest(url).header(HttpHeader.ACCEPT_LANGUAGE, config.language)
                    .header(HttpHeader.USER_AGENT, userAgentSupplier.get())
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                return jsonResponse;
            } else {
                logger.debug("Resolving of {} failed with status {} and response: {}", toBeResolved.toFullString(),
                        statusResponse, jsonResponse);
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Resolving of {} failed with exception {}", toBeResolved.toFullString(), e.getMessage());
        } catch (InterruptedException ie) {
            logger.debug("Resolving of {} interrupted {}", toBeResolved.toFullString(), ie.getMessage());
            Thread.currentThread().interrupt();
        }
        return "";
    }

    @Override
    public String toString() {
        return toBeResolved.toFullString();
    }

    public @Nullable String formatAddress(String jsonResponse) {
        try {
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
        } catch (JSONException e) {
            logger.debug("Could not parse JSON response: {}", e.getMessage());
            return null;
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

    /**
     * Get geo coordinates from JSON response
     *
     * @param jsonResponse from the geocoding API
     * @return String with "lat,lon" or null if not found
     */
    public @Nullable String getGeoCoordinates(String jsonResponse) {
        try {
            JSONArray searchResults = new JSONArray(jsonResponse);
            logger.debug("Geo search found {} results", searchResults.length());
            if (searchResults.length() > 0) {
                JSONObject firstResult = searchResults.getJSONObject(0);
                if (firstResult.has(LATITUDE_KEY) && firstResult.has(LONGITUDE_KEY)) {
                    return firstResult.getString(LATITUDE_KEY) + "," + firstResult.getString(LONGITUDE_KEY);
                }
            }
        } catch (JSONException e) {
            logger.debug("Could not parse JSON response: {}", e.getMessage());
        }
        return null;
    }
}
