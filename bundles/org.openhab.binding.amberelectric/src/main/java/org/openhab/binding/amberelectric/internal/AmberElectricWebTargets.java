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
package org.openhab.binding.amberelectric.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
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

    public AmberElectricWebTargets() {
    }

    public String getSites(String apiKey) throws AmberElectricCommunicationException {
        String getSitesUri = BASE_URI + "sites";
        String response = invoke("GET", getSitesUri, apiKey);
        logger.trace("Received response: \"{}\"", response);
        return response;
    }

    public String getCurrentPrices(String siteid, String apiKey) throws AmberElectricCommunicationException {
        String getCurrentPricesUri = BASE_URI + "sites/" + siteid + "/prices/current?next=288";
        String response = invoke("GET", getCurrentPricesUri, apiKey);
        logger.trace("Received response: \"{}\"", response);
        return response;
    }

    protected Properties getHttpHeaders(String accessToken) {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Authorization", "Bearer " + accessToken);
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    private String invoke(String httpMethod, String uri, String accessToken)
            throws AmberElectricCommunicationException {
        return invoke(httpMethod, uri, accessToken, null, null);
    }

    private String invoke(String httpMethod, String uri, String apiKey, @Nullable InputStream content,
            @Nullable String contentType) throws AmberElectricCommunicationException {
        logger.debug("Calling url: {}", uri);
        @Nullable
        String response;
        try {
            response = HttpUtil.executeUrl(httpMethod, uri, getHttpHeaders(apiKey), content, contentType, TIMEOUT_MS);
        } catch (IOException ex) {
            logger.debug("{}", ex.getLocalizedMessage(), ex);
            // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
            // error check below consistent.
            response = null;
        }

        if (response == null) {
            throw new AmberElectricCommunicationException(
                    String.format("AmberElectric returned no response while invoking %s", uri));
        }
        return response;
    }
}
