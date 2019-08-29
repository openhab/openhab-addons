/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.IOException;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.teslapowerwall.internal.api.BatterySOE;
import org.openhab.binding.teslapowerwall.internal.api.GridStatus;
import org.openhab.binding.teslapowerwall.internal.api.MeterAggregates;
import org.openhab.binding.teslapowerwall.internal.api.Operations;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handles performing the actual HTTP requests for communicating with Tesla Powerwall units.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class TeslaPowerwallWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String getBatterySOEUri;
    private String getGridStatusUri;
    private String getMeterAggregatesUri;
    private String getTokenUri;
    private String getOperationUri;
    private String setOperationUri;
    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallWebTargets.class);

    public TeslaPowerwallWebTargets(String ipAddress) {
        String baseUri = "https://" + ipAddress + "/";
        getBatterySOEUri = baseUri + "api/system_status/soe";
        getGridStatusUri = baseUri + "api/system_status/grid_status";
        getMeterAggregatesUri = baseUri + "api/meters/aggregates";
        getTokenUri = baseUri + "api/login/Basic";
        getOperationUri = baseUri + "api/operation";
        setOperationUri = baseUri + "api/operation";
    }

    public BatterySOE getBatterySOE() throws TeslaPowerwallCommunicationException {
        String response = invoke(getBatterySOEUri);
        return BatterySOE.parse(response);
    }

    public GridStatus getGridStatus() throws TeslaPowerwallCommunicationException {
        String response = invoke(getGridStatusUri);
        return GridStatus.parse(response);
    }

    public MeterAggregates getMeterAggregates() throws TeslaPowerwallCommunicationException {
        String response = invoke(getMeterAggregatesUri);
        return MeterAggregates.parse(response);
    }

    public String getToken(String email, String password) throws TeslaPowerwallCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", "customer");
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("force_sm_off", false);
        logger.debug("logonjson = {}",jsonObject.toString());
        String response = invoke2(getTokenUri, jsonObject.toString());
        JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();
        String token = jsonResponse.get("token").getAsString();
        logger.debug("Token: {}", token);
        return token;
    }

    public Operations getOperations(String token) throws TeslaPowerwallCommunicationException {
        String response;
        Properties headers = new Properties();
        headers.setProperty("Content-Type", token);

        synchronized (this) {
            try {
                response = HttpUtil.executeUrl("GET", getOperationUri, headers, null, null, TIMEOUT_MS);
            } catch (IOException ex) {
                logger.debug("{}", ex.getLocalizedMessage(), ex);
                // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
                // error check below consistent.
                response = null;
            }
        }

        if (response == null) {
            throw new TeslaPowerwallCommunicationException(
                    String.format("Tesla Powerwall returned error while invoking %s", getOperationUri));
        }
        logger.debug("getOperations response = {}", response);
        return Operations.parse(response);
    }

    private String invoke(String uri) throws TeslaPowerwallCommunicationException {
        return invoke(uri, "");
    }

    private String invoke(String uri, String params) throws TeslaPowerwallCommunicationException {
        String uriWithParams = uri + params;
        logger.debug("Calling url: {}", uriWithParams);
        String response;
        synchronized (this) {
            try {
                response = HttpUtil.executeUrl("GET", uriWithParams, TIMEOUT_MS);
            } catch (IOException ex) {
                logger.debug("{}", ex.getLocalizedMessage(), ex);
                // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
                // error check below consistent.
                response = null;
            }
        }

        if (response == null) {
            throw new TeslaPowerwallCommunicationException(
                    String.format("Tesla Powerwall returned error while invoking %s", uriWithParams));
        }
        return response;
    }

    private String invoke2(String uri, String jsonparams) throws TeslaPowerwallCommunicationException {
        logger.debug("Calling url: {}", uri);
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "application/json");
        ByteArrayInputStream input = new ByteArrayInputStream(jsonparams.getBytes(StandardCharsets.UTF_8));
        String response;
        synchronized (this) {
            try {
                response = HttpUtil.executeUrl("POST", uri, headers, input, "application/json", TIMEOUT_MS);
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
