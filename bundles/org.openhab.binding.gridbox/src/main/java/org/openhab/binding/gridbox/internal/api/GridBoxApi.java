/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gridbox.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gridbox.internal.GridBoxConfiguration;
import org.openhab.binding.gridbox.internal.model.LiveData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link GridBoxApi} is responsible for executing the HTTP calls to the GridBox API. Calls are executed
 * synchronously, so the functions should be called from a parallel thread.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxApi {

    public class GridBoxApiSystemNotFoundException extends Exception {

        private static final long serialVersionUID = 2485670225601258718L;

        public GridBoxApiSystemNotFoundException(String message) {
            super(message);
        }
    }

    public class GridBoxApiException extends Exception {

        private static final long serialVersionUID = -5295192044532589122L;

        public GridBoxApiException(String message) {
            super(message);
        }
    }

    public class GridBoxApiAuthenticationException extends Exception {

        private static final long serialVersionUID = 7626147923372849513L;

        public GridBoxApiAuthenticationException(String message) {
            super(message);
        }
    }

    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(5);
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(GridBoxApi.class);
    private final HttpClient httpClient;

    public GridBoxApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetch the ID Token required for the GridBox API calls by querying an OAuth request.
     *
     * @param config The {@link GridBoxConfiguration} to use, must contain valid user email/password
     * @return The fetched ID Token. Will always be non-null, if no exception occurs.
     * @throws IOException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws InterruptedException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws GridBoxApiAuthenticationException Thrown in case of an invalid response of the authorization query,
     *             either if a status code not equal to 200 is returned or the ID Token cannot be parsed from the
     *             response body
     */
    public String getIdToken(GridBoxConfiguration config)
            throws IOException, InterruptedException, GridBoxApiAuthenticationException {
        HttpRequest.BodyPublisher userPublisher = HttpRequest.BodyPublishers.ofString("""
                {
                    "grant_type": "http://auth0.com/oauth/grant-type/password-realm",
                    "username": "%s",
                    "password": "%s",
                    "audience": "my.gridx",
                    "client_id": "oZpr934Ikn8OZOHTJEcrgXkjio0I0Q7b",
                    "scope": "email openid",
                    "realm": "viessmann-authentication-db"
                }
                """.formatted(config.email, config.password));

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://gridx.eu.auth0.com/oauth/token"))
                .POST(userPublisher)
                .timeout(TIMEOUT_DURATION)
                .setHeader("Content-Type", "application/json")
                .build();
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        int status = response.statusCode();
        if (status != 200) {
            logger.debug("Invalid response of authentication request, returned status: {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiAuthenticationException("Authentication request returned an invalid response");
        }

        logger.atTrace().log(() -> "Authentication request returned body: {}".formatted(body));

        Optional<String> idTokenValue = parseIdTokenValue(body);

        if (idTokenValue.isEmpty()) {
            logger.debug("Invalid response of authentication request, returned status: {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiAuthenticationException("Authentication request returned an invalid response");
        }

        return idTokenValue.get();
    }

    /**
     * Parse the ID Token from a JSON response body
     *
     * @param body A JSON response containing an object with an "id_token" attribute
     * @return The parsed "id_token" value, only non-empty if parsing was successful
     */
    public static Optional<String> parseIdTokenValue(String body) {
        String idTokenValue = null;
        JsonElement parentElement = JsonParser.parseString(body);
        if (parentElement.isJsonObject()) {
            JsonElement idTokenElement = parentElement.getAsJsonObject().get("id_token");
            if (idTokenElement != null && idTokenElement.isJsonPrimitive()) {
                idTokenValue = idTokenElement.getAsString();
            }
        }
        return Optional.ofNullable(idTokenValue);
    }

    /**
     * Fetch the System ID required for the GridBox API calls by executing a gateway API query.
     *
     * @param config The {@link GridBoxConfiguration} to use, containing a valid ID token
     * @return The fetched System ID. Will always be non-null, if no exception occurs.
     * @throws IOException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws InterruptedException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws GridBoxApiException Thrown in case of an invalid response of the gateway query, either
     *             if a status code not equal to 200 or 403 is returned or the System ID cannot be parsed from the
     *             response body
     * @throws GridBoxApiAuthenticationException Thrown in case of an invalid response of the authorization query, if a
     *             status code equal to 403 is returned
     */
    public String getSystemId(GridBoxConfiguration config)
            throws IOException, InterruptedException, GridBoxApiAuthenticationException, GridBoxApiException {
        // @formatter:off
        HttpRequest gatewayRequest = HttpRequest.newBuilder(URI.create("https://api.gridx.de/gateways"))
                .GET()
                .timeout(TIMEOUT_DURATION)
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(config.idToken))
                .build();
        // @formatter:on

        HttpResponse<String> response = httpClient.send(gatewayRequest, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        int status = response.statusCode();

        if (status == 403) {
            logger.debug("Gateway request returned access forbidden, returned status: {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiAuthenticationException("Gateway request forbidden");
        } else if (status != 200) {
            logger.debug("Invalid response of gateway request, returned status {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiException("Gateway request returned an invalid response");
        }

        logger.atTrace().log(() -> "Gateway request returned body: {}".formatted(body));

        Optional<String> systemIdValue = parseSystemIdValue(body);
        if (systemIdValue.isEmpty()) {
            logger.debug("Invalid response of gateway request, returned status {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiException("Gateway request returned an invalid response");
        }

        return systemIdValue.get();
    }

    /**
     * Parse the System ID from a JSON response body
     *
     * @param body A JSON response containing the System ID in an "id" attribute
     * @return The parsed "id" value, only non-empty if parsing was successful
     */
    public static Optional<String> parseSystemIdValue(String body) {
        JsonElement jsonObject = JsonParser.parseString(body);
        String systemIdValue = null;
        if (jsonObject.isJsonArray()) {
            JsonElement index0Element = jsonObject.getAsJsonArray().get(0);
            if (index0Element != null && index0Element.isJsonObject()) {
                JsonElement systemElement = index0Element.getAsJsonObject().get("system");
                if (systemElement != null && systemElement.isJsonObject()) {
                    JsonElement idElement = systemElement.getAsJsonObject().get("id");
                    if (idElement != null && idElement.isJsonPrimitive()) {
                        systemIdValue = idElement.getAsString();
                    }
                }
            }
        }
        return Optional.ofNullable(systemIdValue);
    }

    /**
     * Fetch the Live Data from the GridBox API by executing an API query.
     *
     * @param config The {@link GridBoxConfiguration} to use, containing a valid ID token
     * @param responseHandler A function handling the retrieved live data, called if a valid response was received
     *            with a {@link LiveData} object containing the content of the response.
     * @throws IOException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws InterruptedException see {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * @throws GridBoxApiException Thrown in case of an invalid response of the live data query, either
     *             if a status code not equal to 200 or 403 is returned or the {@link LiveData} instance cannot be
     *             parsed from the response body
     * @throws GridBoxApiSystemNotFoundException Thrown in case the query returned status code 404, representing a
     *             non-existing System ID
     * @throws GridBoxApiAuthenticationException Thrown in case of an invalid response of the authorization query if a
     *             status code equal to 403 is returned
     */
    public void retrieveLiveData(GridBoxConfiguration config, Consumer<LiveData> responseHandler)
            throws IOException, InterruptedException, GridBoxApiAuthenticationException,
            GridBoxApiSystemNotFoundException, GridBoxApiException {
        // @formatter:off
        HttpRequest liveDataRequest = HttpRequest
                .newBuilder(URI.create("https://api.gridx.de/systems/%s/live".formatted(config.systemId)))
                .GET()
                .timeout(TIMEOUT_DURATION).setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(config.idToken))
                .build();
        // @formatter:on

        HttpResponse<String> response = httpClient.send(liveDataRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body();
        if (status == 403) {
            logger.debug("Live data request returned access forbidden, status {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiAuthenticationException("Live Data request forbidden");
        } else if (status == 404) {
            logger.debug("Invalid response of live data request, returned status {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiSystemNotFoundException("Live Data request returned an invalid response");
        } else if (status != 200) {
            logger.debug("Invalid response of live data request, returned status {}", status);
            logger.trace("Response body: {}", body);
            throw new GridBoxApiException("Live Data request returned an invalid response");
        }

        JsonElement jsonObject;
        try {
            jsonObject = JsonParser.parseString(body);
        } catch (JsonParseException e) {
            logger.debug("Invalid response of live data request, could not parse JSON body");
            logger.trace("JSON body: {}", body);
            throw new GridBoxApiException("Live Data request returned an invalid response");
        }

        logger.atTrace().log(() -> "Live data request returned body: {}".formatted(jsonObject));

        LiveData liveData = GSON.fromJson(jsonObject, LiveData.class);
        if (liveData != null && !liveData.allValuesZero()) {
            responseHandler.accept(liveData);
        }
    }
}
