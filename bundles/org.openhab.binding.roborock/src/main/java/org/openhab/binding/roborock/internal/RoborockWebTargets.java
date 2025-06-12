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
package org.openhab.binding.roborock.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with the RoborockAPI.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class RoborockWebTargets {
    private static final int TIMEOUT_MS = 30000;
    private static final String BASE_URI = "https://roborock.com/api/";
    private static final String BASE_VEHICLE_URI = BASE_URI + "vehicle/";
    private final Logger logger = LoggerFactory.getLogger(RoborockWebTargets.class);
    private HttpClient httpClient;

    public RoborockWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getVacuumList(String apiKey) throws RoborockCommunicationException, RoborockAuthenticationException {
        return invoke(BASE_URI + "vehicles?api_key=" + apiKey);
    }

    public String getDetailedInformation(String publicID, String apiKey)
            throws RoborockCommunicationException, RoborockAuthenticationException {
        return invoke(BASE_VEHICLE_URI + publicID + "/detailed?api_key=" + apiKey);
    }

    public void sendCommand(String publicID, String apiKey, String command)
            throws RoborockCommunicationException, RoborockAuthenticationException {
        invoke(BASE_VEHICLE_URI + publicID + "/command/" + command + "?api_key=" + apiKey);
        return;
    }

    private String invoke(String uri) throws RoborockCommunicationException, RoborockAuthenticationException {
        logger.debug("Calling url: {}", uri);
        String jsonResponse = "";
        int status = 0;
        try {
            Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            if (logger.isTraceEnabled()) {
                logger.trace("{} request for {}", HttpMethod.GET, uri);
            }
            ContentResponse response = request.send();
            status = response.getStatus();
            jsonResponse = response.getContentAsString();
            logger.trace("JSON response: '{}'", jsonResponse);
            if (status == HttpStatus.UNAUTHORIZED_401) {
                throw new RoborockAuthenticationException("Unauthorized");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new RoborockCommunicationException(
                        String.format("Roborock returned error <%d> while invoking %s", status, uri));
            }
        } catch (TimeoutException | ExecutionException | InterruptedException ex) {
            throw new RoborockCommunicationException(ex.getLocalizedMessage(), ex);
        }

        return jsonResponse;
    }
}
