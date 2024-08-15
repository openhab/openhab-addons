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
package org.openhab.binding.fenecon.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fenecon.internal.FeneconConfiguration;
import org.openhab.binding.fenecon.internal.exception.FeneconAuthenticationException;
import org.openhab.binding.fenecon.internal.exception.FeneconCommunicationException;
import org.openhab.binding.fenecon.internal.exception.FeneconException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Builder baseHttpRequest;

    public FeneconController(FeneconConfiguration config) {
        this.config = config;

        logger.debug("FENECON: initialize REST-API connection to {} with polling interval: {} sec", getBaseUrl(config),
                config.refreshInterval);

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        baseHttpRequest = createBaseHttpRequest(config);
    }

    private String getBasicAuthHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private String getBaseUrl(FeneconConfiguration config) {
        return "http://" + config.hostname + ":" + config.port + "/";
    }

    private Builder createBaseHttpRequest(FeneconConfiguration config) {
        String basicAuth = getBasicAuthHeader("x", config.password);
        return HttpRequest.newBuilder().timeout(Duration.ofSeconds(5)).header("Authorization", basicAuth)
                .header("Content-Type", "application/json").GET();
    }

    /**
     * Queries the data for a specified channel.
     *
     * @param channel Channel to be queried, e.g. _sum/State .
     * @return {@link FeneconResponse}
     * @throws FeneconException is thrown if there are problems with the connection or processing of data to the FENECON
     *             system.
     */
    public FeneconResponse requestChannel(String channel) throws FeneconException {
        try {
            HttpRequest request = baseHttpRequest.uri(new URI(getBaseUrl(config) + "rest/channel/" + channel)).build();
            logger.trace("FENECON - request: {}", request);

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            logger.trace("FENECON - response status code: {} body: {}", response.statusCode(), response.body());

            int statusCode = response.statusCode();
            if (statusCode > 300) {
                // Authentication error
                if (statusCode == 401) {
                    throw new FeneconAuthenticationException(
                            "Authentication on the FENECON system was not possible. Check password.");
                } else {
                    throw new FeneconCommunicationException("Unexpected http status code: " + statusCode);
                }
            } else {
                return createResponseFromJson(JsonParser.parseString(response.body()).getAsJsonObject());
            }
        } catch (IOException | UnsupportedOperationException | InterruptedException err) {
            throw new FeneconCommunicationException("Communication error with FENECON system on channel: " + channel,
                    err);
        } catch (URISyntaxException | JsonSyntaxException err) {
            throw new FeneconCommunicationException("Syntax error on channel: " + channel, err);
        }
    }

    private FeneconResponse createResponseFromJson(JsonObject response) {
        // Example response: {"address":"_sum/EssSoc","type":"INTEGER","accessMode":"RO","text":"Range
        // 0..100","unit":"%","value":99}
        String address = response.get("address").getAsString();
        String text = response.get("text").getAsString();
        String value = response.get("value").getAsString();

        return new FeneconResponse(address, text, value);
    }
}
