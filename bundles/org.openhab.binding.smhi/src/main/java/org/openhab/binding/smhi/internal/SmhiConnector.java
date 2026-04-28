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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * Class for handling http requests to Smhi's API and return values.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiConnector {

    private final Logger logger = LoggerFactory.getLogger(SmhiConnector.class);

    private static final String ACCEPT = "application/json";

    private final HttpClient httpClient;

    public SmhiConnector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Get the reference time (the time when the forecast starts) of the latest published forecast
     * 
     * @return A {@link ZonedDateTime} with the time of the latest forecast.
     */
    public ZonedDateTime getCreatedTime() throws SmhiException {
        logger.debug("Fetching reference time");
        ContentResponse resp = makeRequest(CREATED_TIME_URL);

        if (resp.getStatus() == 200) {
            return Parser.parseCreatedTime(resp.getContentAsString());
        } else {
            throw new SmhiException(resp.getReason());
        }
    }

    /**
     * Get a forecast for the specified WGS84 coordinates.
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return A {@link SmhiTimeSeries} object containing the published forecasts.
     */
    public SmhiTimeSeries getForecast(double lat, double lon) throws SmhiException, PointOutOfBoundsException {
        logger.debug("Fetching new forecast");
        String url = String.format(Locale.ROOT, POINT_FORECAST_URL, lon, lat);
        ContentResponse resp = makeRequest(url);

        switch (resp.getStatus()) {
            case 200:
                try {
                    return Parser.parseTimeSeries(resp.getContentAsString());
                } catch (JsonParseException | DateTimeParseException e) {
                    throw new SmhiException(e);
                }
            case 400:
            case 404:
                throw new PointOutOfBoundsException();
            default:
                throw new SmhiException(resp.getReason());
        }
    }

    public List<ParameterMetadata> getParameterMetadata() throws SmhiException {
        logger.debug("Fetching parameter metadata");
        ContentResponse resp = makeRequest(PARAMETER_METADATA_URL);

        if (resp.getStatus() == 200) {
            try {
                return Parser.parseParameterMetadata(resp.getContentAsString());
            } catch (JsonParseException | DateTimeParseException e) {
                throw new SmhiException(e);
            }
        }
        throw new SmhiException(resp.getReason());
    }

    private ContentResponse makeRequest(String url) throws SmhiException {
        Request req = httpClient.newRequest(url);
        req.accept(ACCEPT);
        ContentResponse resp;
        try {
            resp = req.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SmhiException(e);
        }
        logger.debug("Received response with status {} - {}", resp.getStatus(), resp.getReason());
        return resp;
    }
}
