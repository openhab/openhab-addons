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
package org.openhab.binding.airgradient.internal.communication;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CONTENTTYPE_JSON;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CONTENTTYPE_OPENMETRICS;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CONTENTTYPE_TEXT;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.REQUEST_TIMEOUT;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.binding.airgradient.internal.model.LedMode;
import org.openhab.binding.airgradient.internal.model.LocalConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Helper for doing rest calls to the AirGradient API.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class RemoteAPIController {

    private final Logger logger = LoggerFactory.getLogger(RemoteAPIController.class);

    private final HttpClient httpClient;
    private final Gson gson;
    private final AirGradientAPIConfiguration apiConfig;

    public RemoteAPIController(HttpClient httpClient, Gson gson, AirGradientAPIConfiguration apiConfig) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.apiConfig = apiConfig;
    }

    /**
     * Return list of measures from AirGradient API.
     *
     * @return list of measures
     * @throws AirGradientCommunicationException if unable to communicate with sensor or API.
     */
    public List<Measure> getMeasures() throws AirGradientCommunicationException {
        ContentResponse response = sendRequest(
                RESTHelper.generateRequest(httpClient, RESTHelper.generateMeasuresUrl(apiConfig)));
        if (response != null) {
            String contentType = response.getMediaType();
            logger.trace("Got measurements with status {}: {} ({})", response.getStatus(),
                    response.getContentAsString(), contentType);

            if (HttpStatus.isSuccess(response.getStatus())) {
                String stringResponse = response.getContentAsString().trim();

                if (null != contentType) {
                    switch (contentType) {
                        case CONTENTTYPE_JSON:
                            return JsonParserHelper.parseJson(gson, stringResponse);
                        case CONTENTTYPE_TEXT:
                            return PrometheusParserHelper.parsePrometheus(stringResponse);
                        case CONTENTTYPE_OPENMETRICS:
                            return PrometheusParserHelper.parsePrometheus(stringResponse);
                        default:
                            logger.debug("Unhandled content type returned: {}", contentType);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    public @Nullable LocalConfiguration getConfig() throws AirGradientCommunicationException {
        ContentResponse response = sendRequest(
                RESTHelper.generateRequest(httpClient, RESTHelper.generateConfigUrl(apiConfig)));
        if (response == null) {
            return null;
        }

        logger.trace("Got configuration with status {}: {}", response.getStatus(), response.getContentAsString());

        Type configType = new TypeToken<LocalConfiguration>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), configType);
    }

    public void setConfig(LocalConfiguration config) throws AirGradientCommunicationException {
        Request request = httpClient.newRequest(RESTHelper.generateConfigUrl(apiConfig));
        request.timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        request.method(HttpMethod.PUT);
        request.header(HttpHeader.CONTENT_TYPE, CONTENTTYPE_JSON);
        String configJson = gson.toJson(config);
        logger.debug("Setting configuration: {}", configJson);
        request.content(new StringContentProvider(CONTENTTYPE_JSON, configJson, StandardCharsets.UTF_8));
        sendRequest(request);
    }

    public void setLedMode(String serialNo, String mode) throws AirGradientCommunicationException {
        Request request = httpClient.newRequest(RESTHelper.generateGetLedsModeUrl(apiConfig, serialNo));
        request.timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        request.method(HttpMethod.PUT);
        request.header(HttpHeader.CONTENT_TYPE, CONTENTTYPE_JSON);
        LedMode ledMode = new LedMode();
        ledMode.mode = mode;
        String modeJson = gson.toJson(ledMode);
        logger.debug("Setting LEDS mode for {}: {}", serialNo, modeJson);
        request.content(new StringContentProvider(CONTENTTYPE_JSON, modeJson, StandardCharsets.UTF_8));
        sendRequest(request);
    }

    public void calibrateCo2(String serialNo) throws AirGradientCommunicationException {
        logger.debug("Triggering CO2 calibration for {}", serialNo);
        sendRequest(RESTHelper.generateRequest(httpClient, RESTHelper.generateCalibrationCo2Url(apiConfig, serialNo),
                HttpMethod.POST));
    }

    private @Nullable ContentResponse sendRequest(@Nullable final Request request)
            throws AirGradientCommunicationException {
        if (request == null) {
            throw new AirGradientCommunicationException("Unable to generate request");
        }

        @Nullable
        ContentResponse response = null;
        try {
            response = request.send();
            if (response != null) {
                logger.trace("Response from {} ({}): {}", request.getURI(), response.getStatus(),
                        response.getContentAsString());
                if (!HttpStatus.isSuccess(response.getStatus())) {
                    throw new AirGradientCommunicationException("Returned status code: " + response.getStatus());
                }
            } else {
                throw new AirGradientCommunicationException("No response");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            String message = e.getMessage();
            if (message == null) {
                message = "Communication error";
            }
            throw new AirGradientCommunicationException(message, e);
        }

        return response;
    }
}
