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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.library.types.PointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get coordinates for a given search string using OpenStreetMap (OSM) Nominatim API service
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OSMGeocoding {
    private final Logger logger = LoggerFactory.getLogger(OSMGeocoding.class);

    private HttpClient httpClient;
    private String searchString;

    public OSMGeocoding(String search, HttpClient httpClient) {
        this.searchString = search;
        this.httpClient = httpClient;
    }

    /**
     * Perform search for the given command string and take the first relevant result as geo coordinates
     *
     * @param command string with the search query
     */
    public @Nullable PointType resolve() {
        try {
            String encodedSearch = URLEncoder.encode(searchString, StandardCharsets.UTF_8.toString());
            ContentResponse response = httpClient.newRequest(String.format(SEARCH_URL, encodedSearch))
                    .header("User-Agent", "openHAB Geo Transformation Service").timeout(10, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                PointType geoCoordinates = parse(jsonResponse);
                if (geoCoordinates != null) {
                    return geoCoordinates;
                } else {
                    logger.debug("Geo search doesn't provide coordinates {}", jsonResponse);
                }
            } else {
                logger.debug("Geo search for {} failed with status {} and response: {}", encodedSearch, statusResponse,
                        jsonResponse);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Geo search for {} failed with exception {}", searchString, e.getMessage());
        }
        return null;
    }

    public @Nullable PointType parse(String jsonResponse) {
        JSONArray searchResults = new JSONArray(jsonResponse);
        logger.debug("Geo search found {} results", searchResults.length());
        if (searchResults.length() > 0) {
            JSONObject firstResult = searchResults.getJSONObject(0);
            if (firstResult.has(LATITUDE_KEY) && firstResult.has(LONGITUDE_KEY)) {
                return PointType
                        .valueOf(firstResult.getString(LATITUDE_KEY) + "," + firstResult.getString(LONGITUDE_KEY));
            }
        }
        return null;
    }
}
