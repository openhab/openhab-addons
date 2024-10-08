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
package org.openhab.binding.dirigera.internal.network;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RestAPI} provides easy access towards REST API
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class RestAPI {

    private final Logger logger = LoggerFactory.getLogger(RestAPI.class);
    private HttpClient httpClient;
    private Gateway gateway;

    public RestAPI(HttpClient httpClient, Gateway gateway) {
        this.httpClient = httpClient;
        this.gateway = gateway;
    }

    private Request addAuthorizationHeader(Request sourceRequest) {
        if (!gateway.getToken().isBlank()) {
            return sourceRequest.header(HttpHeader.AUTHORIZATION, "Bearer " + gateway.getToken());
        } else {
            logger.warn("DIRIGERA Cannot operate with token {}", gateway.getToken());
            return sourceRequest;
        }
    }

    public JSONObject readHome() {
        String url = String.format(HOME_URL, gateway.getIpAddress());
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            if (response.getStatus() == 200) {
                return new JSONObject(response.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        return new JSONObject();
    }

    public JSONObject readDevice(String deviceId) {
        String url = String.format(DEVICE_URL, gateway.getIpAddress(), deviceId);
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            if (response.getStatus() == 200) {
                return new JSONObject(response.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        return new JSONObject();
    }
}
