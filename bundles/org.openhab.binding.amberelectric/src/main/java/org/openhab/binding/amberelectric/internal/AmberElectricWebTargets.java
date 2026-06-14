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
package org.openhab.binding.amberelectric.internal;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with the AmberAPI.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class AmberElectricWebTargets {
    private static final int TIMEOUT_MS = 30000;
    private static final String BASE_URI = "https://api.amber.com.au/v1/";
    private final Logger logger = LoggerFactory.getLogger(AmberElectricWebTargets.class);
    private final HttpClient httpClient;

    public AmberElectricWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getSites(String apiKey) throws AmberElectricCommunicationException {
        String getSitesUri = BASE_URI + "sites";
        String response = invoke(getSitesUri, apiKey);
        logger.trace("Received response: \"{}\"", response);
        return response;
    }

    public String getCurrentPrices(String siteid, String apiKey) throws AmberElectricCommunicationException {
        String getCurrentPricesUri = BASE_URI + "sites/" + siteid + "/prices/current?next=288";
        String response = invoke(getCurrentPricesUri, apiKey);
        logger.trace("Received response: \"{}\"", response);
        return response;
    }

    protected Properties getHttpHeaders(String accessToken) {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Authorization", "Bearer " + accessToken);
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    private String invoke(String uri, String accessToken) throws AmberElectricCommunicationException {
        try {
            Request request = httpClient.newRequest(uri).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + accessToken).header("Content-Type", "application/json")
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);

            ContentResponse response = request.send();

            if (response.getStatus() == 401) {
                throw new AmberElectricCommunicationException("Unauthorized: Check API Key");
            }

            return response.getContentAsString();
        } catch (Exception ex) {
            throw new AmberElectricCommunicationException("Error communicating with Amber API", ex);
        }
    }
}
