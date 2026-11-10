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
package org.openhab.binding.tailwind.internal.connector;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tailwind.internal.dto.TailwindControllerData;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link TailwindConnectApi} handles sending commands to the TailWind Controller's
 * API server.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindConnectApi extends TailwindApi {
    private final Logger logger = LoggerFactory.getLogger(TailwindConnectApi.class);

    /**
     * Constructor for handling connections to the TailWind HTTP API
     *
     * @param httpClient common client to use for the TailWind rest API requests
     */
    public TailwindConnectApi(HttpClient httpClient) {
        super(httpClient);
        if (httpClient.isRunning()) {
            logger.debug("---- > Connect APi constructor - http client is running");
        }
    }

    /**
     * Method used by handlers to request device information from the TailWind rest API.
     * <ul>
     * <li>Builds base URL with correct query parameters.</li>
     * <li>Executes the request and if successful returns the parsed JSON response using the data model.</li>
     * </ul>
     *
     * @param apiKey to use when requesting device information from the BloomSky rest API
     * @param query parameters used to build the query parameter used to return the correct observations units
     * @return JSON parsed response if connection was successful
     * @throws TailwindCommunicationException if connection to TailWind API failed
     */
    public TailwindControllerData getTailwindControllerData(Thing thing, String authToken, String body)
            throws TailwindCommunicationException {
        final Request request = getHttpClient().newRequest(getTailwindServerUrl(thing));
        request.method(HttpMethod.POST);
        request.header(TAILWIND_HTTP_HEADER_TOKEN, authToken);
        request.content(new StringContentProvider(body));

        ContentResponse response = executeRequest(request);

        return parseResponse(response, TailwindControllerData.class);
    }

    /**
     * Method executes the call to the TailWind rest API. Key is obtained from the TailWind mobile app
     * found here <a href="http://dashboard.bloomsky.com">Device Owner Portal</a>.
     * <ul>
     * <li>Sets the headers including the API key needed to authorize the request</li>
     * <li>Executes the request and if successful returns the parsed JSON response as a JSON object
     * {@link BloomSkyJsonSensorData} format (i.e. DTO).</li>
     * </ul>
     *
     * @param request is the URL to use with additional request parameters added (e.g. Imperial or International units)
     * @return response (result) from a successful BloomSKy API request
     * @throws BloomSkyCommunicationException
     */
    private ContentResponse executeRequest(final Request request) throws TailwindCommunicationException {
        request.timeout(10, TimeUnit.SECONDS);
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");

        ContentResponse response;
        try {
            response = request.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new TailwindCommunicationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TailwindCommunicationException(e);
        }
        return response;
    }

    /**
     * Method used to extract the device/observations from the TailWind API response from a string to
     * a JSON object.
     *
     * @param <T> - Placeholder for return type (DTO class used to parse the response from the TailWind rest API)
     * @param response - from the HTTP GET request to the TailWind API
     * @param type - name of DTO class to use for the gson.fromJson method call
     * @return A parsed response in JSON format if no errors, otherwise throw an exception with error code
     * @throws TailwindCommunicationException indicating the type of communication error in the API response header
     */
    private <T> T parseResponse(ContentResponse response, Class<T> type) throws TailwindCommunicationException {
        int statusCode = response.getStatus();

        checkForError(response, statusCode);
        try {
            return gson.fromJson(response.getContentAsString(), type);
        } catch (JsonSyntaxException e) {
            throw new TailwindCommunicationException(e);
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
    private void checkForError(ContentResponse response, int statusCode) throws TailwindCommunicationException {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        switch (statusCode) {
            case HttpStatus.NOT_FOUND_404:
                throw new TailwindCommunicationException(statusCode, "Target '" + response.getRequest().getURI()
                        + "' seems to be not available: " + response.getContentAsString());

            case HttpStatus.FORBIDDEN_403:
            case HttpStatus.UNAUTHORIZED_401:
                throw new TailwindUnauthorizedException(statusCode, response.getContentAsString());

            default:
                throw new TailwindCommunicationException(statusCode, response.getContentAsString());
        }
    }

    private String getTailwindServerUrl(Thing thing) throws TailwindCommunicationException {
        String serverURL = "";
        Map<String, String> thingPropertiesMap = thing.getProperties();
        if (thingPropertiesMap.containsKey(TAILWIND_HTTP_SERVER_URL)) {
            String server = thingPropertiesMap.get(TAILWIND_HTTP_SERVER_URL);
            if (server != null && !server.isBlank()) {
                serverURL = TAILWIND_BASE_URL_PART_1 + server + TAILWIND_BASE_URL_PART_2;
            } else {
                serverURL = NOT_FOUND_ERROR;
                throw new TailwindCommunicationException("The Tailwind Thing URL for the Http server " + serverURL
                        + " was not found. Check thing properties for a valid httpServerUrl!!");
            } // If server URL has a value
        } // If server URL was found in the properties for this thing

        ;
        return serverURL;
    }
}
