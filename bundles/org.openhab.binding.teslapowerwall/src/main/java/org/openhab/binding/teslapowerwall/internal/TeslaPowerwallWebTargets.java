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
package org.openhab.binding.teslapowerwall.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teslapowerwall.internal.api.BatterySOE;
import org.openhab.binding.teslapowerwall.internal.api.GridStatus;
import org.openhab.binding.teslapowerwall.internal.api.MeterAggregates;
import org.openhab.binding.teslapowerwall.internal.api.Operations;
import org.openhab.binding.teslapowerwall.internal.api.SystemStatus;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles performing the actual HTTP requests for communicating with Tesla Powerwall units.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class TeslaPowerwallWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String getBatterySOEUri;
    private String getGridStatusUri;
    private String getSystemStatusUri;
    private String getMeterAggregatesUri;
    private String getTokenUri;
    private String getOperationUri;
    private String token = "";
    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallWebTargets.class);

    public TeslaPowerwallWebTargets(String ipAddress) {
        String baseUri = "https://" + ipAddress + "/";
        getBatterySOEUri = baseUri + "api/system_status/soe";
        getGridStatusUri = baseUri + "api/system_status/grid_status";
        getSystemStatusUri = baseUri + "api/system_status";
        getMeterAggregatesUri = baseUri + "api/meters/aggregates";
        getTokenUri = baseUri + "api/login/Basic";
        getOperationUri = baseUri + "api/operation";
    }

    public BatterySOE getBatterySOE(String email, String password) throws TeslaPowerwallCommunicationException {
        String response = invoke(getBatterySOEUri, email, password);
        logger.trace("getBatterySOE response = {}", response);
        return BatterySOE.parse(response);
    }

    public GridStatus getGridStatus(String email, String password) throws TeslaPowerwallCommunicationException {
        String response = invoke(getGridStatusUri, email, password);
        logger.trace("getGridStatus response = {}", response);
        return GridStatus.parse(response);
    }

    public SystemStatus getSystemStatus(String email, String password) throws TeslaPowerwallCommunicationException {
        String response = invoke(getSystemStatusUri, email, password);
        logger.trace("getSystemStatus response = {}", response);
        return SystemStatus.parse(response);
    }

    public MeterAggregates getMeterAggregates(String email, String password)
            throws TeslaPowerwallCommunicationException {
        String response = invoke(getMeterAggregatesUri, email, password);
        logger.trace("getMeterAggregates response = {}", response);
        return MeterAggregates.parse(response);
    }

    public Operations getOperations(String email, String password) throws TeslaPowerwallCommunicationException {
        String response = invoke(getOperationUri, email, password);
        logger.trace("getOperations response = {}", response);
        return Operations.parse(response);
    }

    public String getToken(String email, String password) throws TeslaPowerwallCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", "customer");
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("force_sm_off", false);
        logger.debug("logonjson = {}", jsonObject.toString());
        String response = invoke(getTokenUri, "POST", "Content-Type", "application/json", jsonObject.toString());
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        String token = jsonResponse.get("token").getAsString();
        logger.debug("Token: {}", token);
        return token;
    }

    private String invoke(String uri, String email, String password) throws TeslaPowerwallCommunicationException {
        if (token.isEmpty()) {
            token = getToken(email, password);
        }
        return invoke(uri, "GET", "Authorization", "Bearer " + token, "");
    }

    private String invoke(String uri, String request, String contenttype, String contenttypeparam, String params)
            throws TeslaPowerwallCommunicationException {
        logger.debug("Calling url: {}", uri);
        Properties headers = new Properties();
        headers.setProperty(contenttype, contenttypeparam);
        ByteArrayInputStream input = new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8));
        String response;
        synchronized (this) {
            try {
                response = HttpUtil.executeUrl(request, uri, headers, input, contenttype, TIMEOUT_MS);
            } catch (IOException ex) {
                logger.debug("{}", ex.getLocalizedMessage(), ex);
                // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
                // error check below consistent.
                response = null;
            }
        }

        if (response == null) {
            throw new TeslaPowerwallCommunicationException(
                    String.format("Tesla Powerwall returned error while invoking %s", uri));
        }
        return response;
    }
}
