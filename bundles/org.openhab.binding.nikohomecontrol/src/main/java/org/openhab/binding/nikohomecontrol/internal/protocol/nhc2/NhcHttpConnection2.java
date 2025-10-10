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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * {@link NhcHttpConnection2} manages the HTTP connection to the Connected Controller. This connection is used to
 * retrieve measurements.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcHttpConnection2 {

    private final Logger logger = LoggerFactory.getLogger(NhcHttpConnection2.class);

    private final HttpClient httpClient;
    private String token;
    private String hostname;
    private String measurementsBaseUrl;

    private static final Gson GSON = new Gson();

    private static final long TIMEOUT_MS = 1000;

    NhcHttpConnection2(HttpClient httpClient, String cocoAddress, String token) {
        this.httpClient = httpClient;
        this.token = token;
        this.hostname = cocoAddress;
        measurementsBaseUrl = "https://" + cocoAddress + NikoHomeControlConstants.NHC_MEASUREMENTS_BASEURL;
    }

    public @Nullable String getMeasurements(String deviceUuid, LocalDateTime start, LocalDateTime end) {
        String intervalStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String intervalEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String url = measurementsBaseUrl + "/" + deviceUuid + "/total?Aggregation=sum&IntervalStart=" + intervalStart
                + "&IntervalEnd=" + intervalEnd;
        logger.trace("Get Total Measurements: {}", url);
        Request request = httpClient.newRequest(url);
        ContentResponse response;
        try {
            response = request.method(HttpMethod.GET).header(HttpHeader.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeader.HOST, hostname).header(HttpHeader.ACCEPT, "application/json")
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error with query {}", url);
            return null;
        }

        return response(url, response);
    }

    private @Nullable String response(String url, ContentResponse response) {
        int status = response.getStatus();
        String responseString = response.getContentAsString();
        if (status == 200) {
            logger.debug("Query {}, response: {}", url, responseString);
            return responseString;
        }

        JsonObject jsonObject = GSON.fromJson(responseString, JsonObject.class);
        String message = null;
        String messageDetail = null;
        if (jsonObject != null) {
            if (jsonObject.has("Message") && !jsonObject.get("Message").isJsonNull()) {
                message = jsonObject.get("Message").getAsString();
            }
            if (jsonObject.has("MessageDetail") && !jsonObject.get("MessageDetail").isJsonNull()) {
                messageDetail = jsonObject.get("MessageDetail").getAsString();
            }
        }
        logger.warn("Error query {}: status {}, message {}, detail {}", url, status, message, messageDetail);
        return null;
    }
}
