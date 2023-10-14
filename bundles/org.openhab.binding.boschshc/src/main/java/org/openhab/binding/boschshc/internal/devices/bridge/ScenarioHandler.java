/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Handler for executing a scenario.
 *
 * @author Patrick Gell - Initial contribution
 *
 */
@NonNullByDefault
public class ScenarioHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Scenario> availableScenarios;

    protected ScenarioHandler(Map<String, Scenario> availableScenarios) {
        this.availableScenarios = Objects.requireNonNullElseGet(availableScenarios, HashMap::new);
    }

    public void executeScenario(final @Nullable BoschHttpClient httpClient, final String scenarioName) {
        assert httpClient != null;
        if (!availableScenarios.containsKey(scenarioName)) {
            updateScenarios(httpClient);
        }
        final Scenario scenario = this.availableScenarios.get(scenarioName);
        if (scenario != null) {
            sendRequest(HttpMethod.POST,
                    httpClient.getBoschSmartHomeUrl(String.format("scenarios/%s/triggers", scenario.id)), httpClient);
        } else {
            logger.debug("scenario '{}' not found on the Bosch Controller", scenarioName);
        }
    }

    private void updateScenarios(final @Nullable BoschHttpClient httpClient) {
        if (httpClient != null) {
            final String result = sendRequest(HttpMethod.GET, httpClient.getBoschSmartHomeUrl("scenarios"), httpClient);
            try {
                Scenario[] scenarios = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(result, Scenario[].class);
                if (scenarios != null) {
                    for (Scenario scenario : scenarios) {
                        availableScenarios.put(scenario.name, scenario);
                    }
                }
            } catch (JsonSyntaxException e) {
                logger.debug("response from SHC could not be parsed: {}", result, e);
            }
        }
    }

    private String sendRequest(final HttpMethod method, final String url, final BoschHttpClient httpClient) {
        try {
            final Request request = httpClient.createRequest(url, method);
            final ContentResponse response = request.send();
            switch (HttpStatus.getCode(response.getStatus())) {
                case OK -> {
                    return response.getContentAsString();
                }
                case NOT_FOUND, METHOD_NOT_ALLOWED -> logger.debug("{} - {} failed with {}: {}", method, url,
                        response.getStatus(), response.getContentAsString());
            }
        } catch (InterruptedException e) {
            logger.debug("scenario call was interrupted", e);
        } catch (TimeoutException e) {
            logger.debug("scenarion call timed out", e);
        } catch (ExecutionException e) {
            logger.debug("exception occurred during scenario call", e);
        }
        return "";
    }
}
