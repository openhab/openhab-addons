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
package org.openhab.binding.teslascope.internal;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslascope.internal.api.DetailedInformation;
import org.openhab.core.io.net.http.HttpUtil;
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
    private static final String BASE_URI = "https://teslascope.com/api/vehicle/";
    private final Logger logger = LoggerFactory.getLogger(TeslascopeWebTargets.class);

    public TeslascopeWebTargets() {
    }

    public DetailedInformation getDetailedInformation(String publicID, String apiKey)
            throws TeslascopeCommunicationException {
        String getDetailedInformationUri = BASE_URI + publicID + "/detailed?api_key=" + apiKey;
        String response = invoke(getDetailedInformationUri);
        logger.trace("Received response: \"{}\"", response);
        return DetailedInformation.parse(response);
    }

    public void sendCommand(String publicID, String apiKey, String command) throws TeslascopeCommunicationException {
        String sendCommandUri = BASE_URI + publicID + "/command/" + command + "?api_key=" + apiKey;
        String response = invoke(sendCommandUri);
        logger.trace("Received response: \"{}\"", response);
        return;
    }

    protected Properties getHttpHeaders() {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    private String invoke(String uri) throws TeslascopeCommunicationException {
        logger.debug("Calling url: {}", uri);
        @Nullable
        String response;
        try {
            response = HttpUtil.executeUrl("GET", uri, getHttpHeaders(), null, null, TIMEOUT_MS);
        } catch (IOException ex) {
            logger.debug("{}", ex.getLocalizedMessage(), ex);
            // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
            // error check below consistent.
            response = null;
        }

        if (response == null) {
            throw new TeslascopeCommunicationException(
                    String.format("Teslascope returned no response while invoking %s, check config", uri));
        }
        return response;
    }
}
