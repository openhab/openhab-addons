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
package org.openhab.binding.opengarage.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.opengarage.internal.api.Enums.OpenGarageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Opengarage units.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class OpenGarageWebTargets {
    public static final int DEFAULT_TIMEOUT_MS = 30000;

    private String getControllerVariablesUri;
    private String changeControllerVariablesUri;
    private final Logger logger = LoggerFactory.getLogger(OpenGarageWebTargets.class);
    private int timeoutMs;
    private final HttpClient httpClient;

    public OpenGarageWebTargets(HttpClient httpClient, String ipAddress, long port, String password, int timeoutMs) {
        this.httpClient = httpClient;
        String baseUri = "http://" + ipAddress + ":" + port + "/";
        this.timeoutMs = timeoutMs;
        this.getControllerVariablesUri = baseUri + "jc";
        this.changeControllerVariablesUri = baseUri + "cc?dkey=" + password;
    }

    public String getControllerVariables() throws OpenGarageCommunicationException {
        return invoke(getControllerVariablesUri);
    }

    public void setControllerVariables(OpenGarageCommand request) throws OpenGarageCommunicationException {
        logger.debug("Received request: {}", request);

        String queryParams = switch (request) {
            case OPEN -> "&open=1";
            case CLOSE -> "&close=1";
            case CLICK -> "&click=1";
        };

        invoke(changeControllerVariablesUri, queryParams);
    }

    private String invoke(String uri) throws OpenGarageCommunicationException {
        return invoke(uri, "");
    }

    private String invoke(String uri, String params) throws OpenGarageCommunicationException {
        String uriWithParams = uri + params;
        logger.debug("Calling url: {}", uriWithParams);
        String response;

        synchronized (this) {
            try {
                ContentResponse contentResponse = httpClient.newRequest(uriWithParams).method(HttpMethod.GET)
                        .timeout(this.timeoutMs, TimeUnit.MILLISECONDS).send();

                if (contentResponse.getStatus() != HttpStatus.OK_200) {
                    throw new OpenGarageCommunicationException(
                            String.format("OpenGarage controller returned HTTP %d while invoking %s",
                                    contentResponse.getStatus(), uriWithParams));
                }

                response = contentResponse.getContentAsString();

            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                logger.debug("{}", ex.getLocalizedMessage(), ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new OpenGarageCommunicationException(
                        String.format("OpenGarage controller returned error while invoking %s", uriWithParams), ex);
            }
        }

        return response;
    }
}
