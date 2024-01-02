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
package org.openhab.binding.boschshc.internal.devices.bridge;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for executing a scenario.
 *
 * @author Patrick Gell - Initial contribution
 *
 */
@NonNullByDefault
public class ScenarioHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected ScenarioHandler() {
    }

    public void triggerScenario(final BoschHttpClient httpClient, final String scenarioName) {
        final Scenario[] scenarios;
        try {
            scenarios = getAvailableScenarios(httpClient);
        } catch (BoschSHCException e) {
            logger.debug("unable to read the available scenarios from Bosch Smart Home Conteroller", e);
            return;
        }
        final Optional<Scenario> scenario = Arrays.stream(scenarios).filter(s -> s.name.equals(scenarioName))
                .findFirst();
        if (scenario.isPresent()) {
            sendPOSTRequest(httpClient.getBoschSmartHomeUrl(String.format("scenarios/%s/triggers", scenario.get().id)),
                    httpClient);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Scenario '{}' was not found in the list of available scenarios {}", scenarioName,
                        prettyLogScenarios(scenarios));
            }
        }
    }

    private Scenario[] getAvailableScenarios(final BoschHttpClient httpClient) throws BoschSHCException {
        final Request request = httpClient.createRequest(httpClient.getBoschSmartHomeUrl("scenarios"), HttpMethod.GET);
        try {
            return httpClient.sendRequest(request, Scenario[].class, Scenario::isValid, null);
        } catch (InterruptedException e) {
            logger.debug("Scenario call was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            logger.debug("Scenario call timed out", e);
        } catch (ExecutionException e) {
            logger.debug("Exception occurred during scenario call", e);
        }

        return new Scenario[] {};
    }

    private void sendPOSTRequest(final String url, final BoschHttpClient httpClient) {
        try {
            final Request request = httpClient.createRequest(url, HttpMethod.POST);
            final ContentResponse response = request.send();
            if (HttpStatus.ACCEPTED_202 != response.getStatus()) {
                logger.debug("{} - {} failed with {}: {}", HttpMethod.POST, url, response.getStatus(),
                        response.getContentAsString());
            }
        } catch (InterruptedException e) {
            logger.debug("Scenario call was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            logger.debug("Scenario call timed out", e);
        } catch (ExecutionException e) {
            logger.debug("Exception occurred during scenario call", e);
        }
    }

    private String prettyLogScenarios(final Scenario[] scenarios) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Scenario scenario : scenarios) {
            builder.append("\n  ");
            builder.append(scenario);
        }
        builder.append("\n]");
        return builder.toString();
    }
}
