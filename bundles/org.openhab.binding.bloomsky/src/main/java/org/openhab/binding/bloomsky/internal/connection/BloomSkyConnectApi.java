/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.connection;

import static java.util.stream.Collectors.joining;
import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bloomsky.internal.dto.BloomSkyJsonSensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link BloomSkyConnectAPI} allows access to the BloomSky rest API by the handlers.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public class BloomSkyConnectApi extends BloomSkyApi {

    private final Logger logger = LoggerFactory.getLogger(BloomSkyConnectApi.class);

    /**
     * Constructor for handler connections to the BloomSky API
     *
     * @param httpClient common client to use for BloomSky rest API GET requests
     */
    public BloomSkyConnectApi(HttpClient httpClient) {
        super(httpClient);
    }

    /**
     * Method overridden from {@link BloomSkyApi} to retrieve the base URL for the BloomSky API.
     *
     * @return Base URL to use for the BloomSky API
     */
    @Override
    protected String getBaseUrl() {
        return BASE_API_URL;
    }

    /**
     * Method used to by handlers to request device information from the BloomSky rest API.
     * <ul>
     * <li>Builds base URL with correct unit query parameter using the display units from Bridge configuration.</li>
     * <li>Executes the request and if successful returns the parsed JSON response using the data model.</li>
     * </ul>
     *
     * @param apiKey to use when requesting device information from the BloomSky rest API
     * @param displayUnits used to build the query parameter used to return the correct observations units
     * @return JSON parsed response if connection was successful
     * @throws BloomSkyCommunicationException if connection to BloomSky API failed
     */
    public BloomSkyJsonSensorData[] getSkyDeviceData(String apiKey, String displayUnits)
            throws BloomSkyCommunicationException {
        String url = buildURL(getBaseUrl(), setRequestParams(displayUnits));
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.GET);

        ContentResponse response = executeRequest(apiKey, request);

        return parseResponse(response, BloomSkyJsonSensorData[].class);
    }

    /**
     * Method executes the call to the BloomSky rest API Key is obtained from the BloomSky Device Owner portal
     * found here <a href="http://dashboard.bloomsky.com">Device Owner Portal</a>.
     * <ul>
     * <li>Sets the headers including the API key needed to authorize the request</li>
     * <li>Executes the request and if successful returns the parsed JSON response as a JSON object
     * {@link BloomSkyJsonSensorData} format (i.e. DTO).</li>
     * </ul>
     *
     * @param apiKey found in the Bridge configuration
     * @param request is the URL to use with additional request parameters added (e.g. Imperial or International units)
     * @return response (result) from a successful BloomSKy API request
     * @throws BloomSkyCommunicationException
     */
    private ContentResponse executeRequest(String apiKey, final Request request) throws BloomSkyCommunicationException {
        request.timeout(10, TimeUnit.SECONDS);

        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.AUTHORIZATION, apiKey);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");

        ContentResponse response;
        try {
            response = request.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new BloomSkyCommunicationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BloomSkyCommunicationException(e);
        }
        return response;
    }

    /**
     * Method used to add the query parameters to the base URL (https://api.bloomsky.com/api/skydata/)
     *
     * @param url - base URL to use
     * @param requestParams - list of parameters in key, value format to append to the URL
     * @return Final URL containing base URL with query parameters
     */
    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.entrySet().stream().map(e -> e.getKey() + "=" + encodeParam(e.getValue()))
                .collect(joining("&", url + "?", ""));
    }

    /**
     * Build query parameter(s) for BloomSky API request
     *
     * @param displayUnits - used to identify the correct query parameter for URL
     * @return An array of query parameters for BloomSky API call
     */
    private Map<String, String> setRequestParams(String displayUnits) {
        Map<String, String> params = new HashMap<>();

        // Only one parameter exists: requested display units - Imperial = "", Metric = "intl"
        if (METRIC_UNITS.equals(displayUnits)) {
            params.put(PARAM_UNITS, "intl"); // return sensor data in metric units (Rest of World)
        } else {
            params.put(PARAM_UNITS, ""); // return sensor data in Imperial units (USA)
        }
        return params;
    }

    /**
     * Encode the query string parameters to ensure a valid URL is used to send the request
     * to the BloomSky API.
     *
     * @param value - the query string to be appended to the base URL
     * @return A properly encode query string
     */
    private String encodeParam(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException occurred during execution: {}", e.getLocalizedMessage(), e);
            return "";
        }
    }

    /**
     * Method used to extract the device/observations from the BloomSky API response from a string to
     * a JSON object.
     *
     * @param <T> - Placeholder for return type (DTO class used to parse the response from the BloomSky rest API)
     * @param response - from the HTTP GET request to the BloomSky API
     * @param type - name of DTO class to use for the gson.fromJson method call
     * @return A parsed response in JSON format if no errors, otherwise throw an exception with error code
     * @throws BloomSkyCommunicationException indicating the type of communication error in the API response header
     */
    private <T> T parseResponse(ContentResponse response, Class<T> type) throws BloomSkyCommunicationException {
        int statusCode = response.getStatus();

        checkForError(response, statusCode);
        try {
            return gson.fromJson(response.getContentAsString(), type);
        } catch (JsonSyntaxException e) {
            throw new BloomSkyCommunicationException(e);
        }
    }

    /**
     * Method used to determine if the BloomSky API response is valid, if not, provide additional details
     * in the log explaining what the problem might be.
     *
     * @param response - header/body information from the GET request to the BloomSKy API
     * @param statusCode - from the header to catch typical errors and report them to the log
     * @throws BloomSkyCommunicationException if there is an error requesting information from the BloomSKy API
     */
    private void checkForError(ContentResponse response, int statusCode) throws BloomSkyCommunicationException {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        switch (statusCode) {
            case HttpStatus.NOT_FOUND_404:
                throw new BloomSkyCommunicationException(statusCode, "Target '" + response.getRequest().getURI()
                        + "' seems to be not available: " + response.getContentAsString());

            case HttpStatus.FORBIDDEN_403:
            case HttpStatus.UNAUTHORIZED_401:
                throw new BloomSkyUnauthorizedException(statusCode, response.getContentAsString());

            default:
                throw new BloomSkyCommunicationException(statusCode, response.getContentAsString());
        }
    }
}
