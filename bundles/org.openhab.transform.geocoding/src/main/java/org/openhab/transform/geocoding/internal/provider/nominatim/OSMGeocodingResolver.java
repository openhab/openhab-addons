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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.GeocodingResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OSMGeocodingResolver} is the geocding resolver for Nomination / OpenStreetMap API. A given String will be
 * resolved into geo coordinates.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OSMGeocodingResolver extends GeocodingResolver {
    private final Logger logger = LoggerFactory.getLogger(OSMGeocodingResolver.class);

    private @Nullable String searchString;

    public OSMGeocodingResolver(State state, GeoProfileConfig config, HttpClient httpClient) {
        super(state, config, httpClient);
        if (state instanceof StringType stringType) {
            searchString = stringType.toFullString();
        }
    }

    /**
     * Perform search for the given command string and take the first relevant result as geo coordinates
     *
     * @param command string with the search query
     */
    @Override
    public void resolve() {
        String localSearchString = searchString;
        if (localSearchString == null) {
            logger.info("State {} isn't a location and cannot be reolved into an address", toBeResolved.toFullString());
            return;
        }
        if (isResolved()) {
            logger.trace("Location {} is already resolved {}", toBeResolved.toFullString(), getResolved());
            return;
        }
        try {
            String encodedSearch = URLEncoder.encode(searchString, StandardCharsets.UTF_8.toString());
            ContentResponse response = httpClient.newRequest(String.format(SEARCH_URL, encodedSearch))
                    .header(HttpHeader.ACCEPT_LANGUAGE, config.language)
                    .header(HttpHeader.USER_AGENT, userAgentSupplier.get())
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int statusResponse = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (statusResponse == HttpStatus.OK_200) {
                resolvedString = parse(jsonResponse);
                if (!isResolved()) {
                    logger.debug("Geo search doesn't provide coordinates {}", jsonResponse);
                }
            } else {
                logger.debug("Geo search for {} failed with status {} and response: {}", encodedSearch, statusResponse,
                        jsonResponse);
            }
        } catch (TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Geo search for {} failed with exception {}", searchString, e.getMessage());
        } catch (InterruptedException ie) {
            logger.debug("Geo search interrupeted for {} failed with exception {}", searchString, ie.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public @Nullable String parse(String jsonResponse) {
        JSONArray searchResults = new JSONArray(jsonResponse);
        logger.debug("Geo search found {} results", searchResults.length());
        if (searchResults.length() > 0) {
            JSONObject firstResult = searchResults.getJSONObject(0);
            if (firstResult.has(LATITUDE_KEY) && firstResult.has(LONGITUDE_KEY)) {
                return firstResult.getString(LATITUDE_KEY) + "," + firstResult.getString(LONGITUDE_KEY);
            }
        }
        return null;
    }
}
