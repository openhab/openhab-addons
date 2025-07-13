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
package org.openhab.binding.fenecon.internal.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fenecon.internal.FeneconConfiguration;
import org.openhab.binding.fenecon.internal.exception.FeneconAuthenticationException;
import org.openhab.binding.fenecon.internal.exception.FeneconCommunicationException;
import org.openhab.binding.fenecon.internal.exception.FeneconException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FeneconController} class provides API access to the FENECON system.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconController {

    private final Logger logger = LoggerFactory.getLogger(FeneconController.class);

    private final FeneconConfiguration config;
    private final HttpClient httpClient;

    public FeneconController(FeneconConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;

        logger.debug("FENECON: initialize REST-API connection to {} with polling interval: {} sec", getBaseUrl(config),
                config.refreshInterval);

        // Set BasicAuthentication for all requests on the http connection
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(getBaseUrl(config));
        auth.addAuthenticationResult(new BasicAuthentication.BasicResult(uri, "x", config.password));
    }

    private String getBaseUrl(FeneconConfiguration config) {
        return "http://" + config.hostname + ":" + config.port + "/";
    }

    /**
     * Queries the data for a specified channel group.
     *
     * @param channel Channel group to be queried, e.g. _sum/(State|EssSoc) .
     * @return {@link FeneconResponse} can be optional if values are not available.
     * @throws FeneconException is thrown if there are problems with the connection or processing of data to the FENECON
     *             system.
     */
    public List<FeneconResponse> requestChannel(String channel) throws FeneconException {
        try {
            URI uri = new URI(getBaseUrl(config) + "rest/channel/" + channel);
            logger.trace("FENECON - uri: {}", uri);

            Request request = httpClient.newRequest(uri).timeout(10, TimeUnit.SECONDS).method(HttpMethod.GET);
            logger.trace("FENECON - request: {}", request);

            ContentResponse response = request.send();
            logger.trace("FENECON - response status code: {} body: {}", response.getStatus(),
                    response.getContentAsString());

            int statusCode = response.getStatus();
            if (statusCode > 300) {
                if (statusCode == 401) { // Authentication error
                    throw new FeneconAuthenticationException(
                            "Authentication on the FENECON system was not possible. Check password.");
                } else if (statusCode == 404) { // Channel-URL not supported
                    logger.debug("Channel request '{}' not possible, is not supported by the FENECON system.", channel);
                    return List.of();
                } else {
                    throw new FeneconCommunicationException("Unexpected http status code: " + statusCode);
                }
            } else {
                return createResponseFromJson(JsonParser.parseString(response.getContentAsString()));
            }
        } catch (TimeoutException | ExecutionException | UnsupportedOperationException | InterruptedException err) {
            throw new FeneconCommunicationException(
                    "Communication error: " + err.getMessage() + " with FENECON system on channel: " + channel, err);
        } catch (URISyntaxException | IllegalStateException | JsonSyntaxException err) {
            throw new FeneconCommunicationException("Syntax error: " + err.getMessage() + " on channel: " + channel,
                    err);
        }
    }

    private List<FeneconResponse> createResponseFromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            return createResponseFromJsonArray(jsonElement.getAsJsonArray());
        } else if (jsonElement.isJsonObject()) {
            return createResponseFromJsonObject(jsonElement.getAsJsonObject());
        } else {
            throw new IllegalStateException("Unexpected response format: " + jsonElement);
        }
    }

    private List<FeneconResponse> createResponseFromJsonArray(JsonArray jsonArray) {
        // Example response: [{"address":"_sum/EssSoc","type":"INTEGER","accessMode":"RO","text":"Range
        // 0..100","unit":"%","value":99}]
        List<FeneconResponse> result = new ArrayList<>();
        for (JsonElement each : jsonArray) {
            if (each.isJsonObject()) {
                result.addAll(createResponseFromJsonObject(each.getAsJsonObject()));
            }
        }
        return result;
    }

    private List<FeneconResponse> createResponseFromJsonObject(JsonObject jsonObject) {
        // Example response: {"address":"_sum/EssSoc","type":"INTEGER","accessMode":"RO","text":"Range
        // 0..100","unit":"%","value":99}
        List<FeneconResponse> result = new ArrayList<>();
        convertJsonObjectToResponse(jsonObject).ifPresent(result::add);
        return result;
    }

    private Optional<FeneconResponse> convertJsonObjectToResponse(JsonObject jsonObject) {
        if (jsonObject.get("value").isJsonNull()) {
            // Example problem response:
            // {"address":"_sum/EssSoc","type":"INTEGER","accessMode":"RO","text":"Range
            // 0..100","unit":"%","value":null}
            return Optional.empty();
        }

        String address = jsonObject.get("address").getAsString();
        String text = jsonObject.get("text").getAsString();
        String value = jsonObject.get("value").getAsString();

        return Optional.of(new FeneconResponse(address, text, value));
    }
}
