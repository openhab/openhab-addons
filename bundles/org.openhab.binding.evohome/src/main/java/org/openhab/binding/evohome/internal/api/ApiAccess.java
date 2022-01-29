/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides access to (an optionally OAUTH based) API. Makes sure that all the necessary headers are set.
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class ApiAccess {
    private static final int REQUEST_TIMEOUT_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(ApiAccess.class);
    private final HttpClient httpClient;
    private final Gson gson;

    private Authentication authenticationData;
    private String applicationId;

    public ApiAccess(HttpClient httpClient) {
        this.gson = new GsonBuilder().create();
        this.httpClient = httpClient;
    }

    /**
     * Sets the authentication details on the type
     *
     * @param authentication The authentication details to apply
     */
    public void setAuthentication(Authentication authentication) {
        authenticationData = authentication;
    }

    /**
     * Gets the current authentication details of the type
     *
     * @return The current authentication details
     */
    public Authentication getAuthentication() {
        return authenticationData;
    }

    /**
     * Sets the application id on the type
     *
     * @param applicationId The application id to apply
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Issues an HTTP request on the API's URL. Makes sure that the request is correctly formatted.
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param headers The optional additional headers to apply, can be null
     * @param requestData The optional request data to use, can be null
     * @param contentType The content type to use with the request data. Required when using requestData
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    public <TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers, String requestData,
            String contentType, Class<TOut> outClass) throws TimeoutException {
        TOut retVal = null;
        logger.debug("Requesting: [{}]", url);

        try {
            Request request = httpClient.newRequest(url).method(method);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.header(header.getKey(), header.getValue());
                }
            }

            if (requestData != null) {
                request.content(new StringContentProvider(requestData), contentType);
            }

            ContentResponse response = request.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

            logger.debug("Response: {}", response);
            logger.debug("\n{}\n{}", response.getHeaders(), response.getContentAsString());

            if ((response.getStatus() == HttpStatus.OK_200) || (response.getStatus() == HttpStatus.ACCEPTED_202)) {
                String reply = response.getContentAsString();

                if (outClass != null) {
                    retVal = new Gson().fromJson(reply, outClass);
                }
            }
        } catch (ExecutionException e) {
            logger.debug("Error in handling request: ", e);
        } catch (InterruptedException e) {
            logger.debug("Handling request interrupted: ", e);
            Thread.currentThread().interrupt();
        }

        return retVal;
    }

    /**
     * Issues an HTTP GET request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param url The URL to query
     * @param outClass The type of the requested result
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    public <TOut> TOut doAuthenticatedGet(String url, Class<TOut> outClass) throws TimeoutException {
        return doAuthenticatedRequest(HttpMethod.GET, url, null, outClass);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param url The URL to query
     * @param requestContainer The object to use as JSON data for the request
     * @throws TimeoutException Thrown when a request times out
     */
    public void doAuthenticatedPut(String url, Object requestContainer) throws TimeoutException {
        doAuthenticatedRequest(HttpMethod.PUT, url, requestContainer, null);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param headers The optional additional headers to apply, can be null
     * @param requestContainer The object to use as JSON data for the request
     * @param outClass The type of the requested result
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    private <TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers, Object requestContainer,
            Class<TOut> outClass) throws TimeoutException {
        String json = null;
        if (requestContainer != null) {
            json = this.gson.toJson(requestContainer);
        }

        return doRequest(method, url, headers, json, "application/json", outClass);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input and
     * using the authentication applied to the type.
     * Makes sure that the request is correctly formatted.*
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param requestContainer The object to use as JSON data for the request
     * @param outClass The type of the requested result
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    private <TOut> TOut doAuthenticatedRequest(HttpMethod method, String url, Object requestContainer,
            Class<TOut> outClass) throws TimeoutException {
        Map<String, String> headers = null;
        if (authenticationData != null) {
            headers = new HashMap<>();

            headers.put("Authorization", "Bearer " + authenticationData.getAccessToken());
            headers.put("applicationId", applicationId);
            headers.put("Accept",
                    "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
        }

        return doRequest(method, url, headers, requestContainer, outClass);
    }
}
