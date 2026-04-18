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
package org.openhab.binding.teslascope.internal;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with the TeslascopeAPI.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class TeslascopeWebTargets {
    private static final int TIMEOUT_MS = 30000;
    private static final int MAX_RETRIES = 3;
    private static final String BASE_URI = "https://teslascope.com/api/";
    private static final String BASE_VEHICLE_URI = BASE_URI + "vehicle/";
    private final Logger logger = LoggerFactory.getLogger(TeslascopeWebTargets.class);
    private HttpClient httpClient;

    public TeslascopeWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getVehicleList(String apiKey, String personalAccessToken)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        if (personalAccessToken.isBlank()) {
            return invoke(BASE_URI + "vehicles?api_key=" + apiKey, "");
        } else {
            return invoke(BASE_URI + "vehicles", personalAccessToken);
        }
    }

    public String getDetailedInformation(String publicID, String apiKey, String personalAccessToken)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        if (personalAccessToken.isBlank()) {
            return invoke(BASE_VEHICLE_URI + publicID + "/detailed?api_key=" + apiKey, "");
        } else {
            return invoke(BASE_VEHICLE_URI + publicID + "/detailed", personalAccessToken);
        }
    }

    public void sendCommand(String publicID, String apiKey, String personalAccessToken, String command)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        if (personalAccessToken.isBlank()) {
            invoke(BASE_VEHICLE_URI + publicID + "/command/" + command + "?api_key=" + apiKey, "");
        } else {
            invoke(BASE_VEHICLE_URI + publicID + "/command/" + command, personalAccessToken);
        }
        return;
    }

    public void sendCommand(String publicID, String apiKey, String personalAccessToken, String command, String params)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        if (personalAccessToken.isBlank()) {
            invoke(BASE_VEHICLE_URI + publicID + "/command/" + command + "?api_key=" + apiKey + params, "");
        } else {
            invoke(BASE_VEHICLE_URI + publicID + "/command/" + command + "?" + params, personalAccessToken);
        }
        return;
    }

    private String invoke(String uri, String personalAccessToken)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        logger.debug("Calling url: {}", uri);
        String jsonResponse = "";
        int status = 0;

        for (int retryCounter = 1; retryCounter <= MAX_RETRIES; retryCounter++) {
            try {
                Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(TIMEOUT_MS,
                        TimeUnit.MILLISECONDS);
                if (!personalAccessToken.isBlank()) {
                    request.header(HttpHeader.AUTHORIZATION.asString(), "Bearer " + personalAccessToken);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {}", HttpMethod.GET, uri);
                }
                ContentResponse response = request.send();
                status = response.getStatus();
                if (HttpStatus.isSuccess(status)) {
                    jsonResponse = response.getContentAsString();
                    logger.trace("JSON response: '{}'", jsonResponse);
                } else {
                    switch (status) {
                        case HttpStatus.UNAUTHORIZED_401:
                            throw new TeslascopeAuthenticationException("Unauthorized");
                        case HttpStatus.INTERNAL_SERVER_ERROR_500:
                        case HttpStatus.BAD_GATEWAY_502:
                            logger.debug("Teslascope returned {}, retrying", status);
                            Thread.sleep(2000);
                            break;
                        default:
                            throw new TeslascopeCommunicationException(
                                    String.format("Teslascope returned error <%d> while invoking %s", status, uri));
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new TeslascopeCommunicationException(ex.getLocalizedMessage(), ex);
            } catch (TimeoutException | ExecutionException ex) {
                throw new TeslascopeCommunicationException(ex.getLocalizedMessage(), ex);
            }
        }
        return jsonResponse;
    }
}
