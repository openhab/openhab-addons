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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.RawType;
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

    public synchronized JSONObject readHome() {
        JSONObject statusObject = new JSONObject();
        String url = String.format(HOME_URL, gateway.getIpAddress());
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200) {
                return new JSONObject(response.getContentAsString());
            } else {
                statusObject.put(PROPERTY_HTTP_ERROR_STATUS, responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        return statusObject;
    }

    public synchronized JSONObject readDevice(String deviceId) {
        JSONObject statusObject = new JSONObject();
        String url = String.format(DEVICE_URL, gateway.getIpAddress(), deviceId);
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200) {
                return new JSONObject(response.getContentAsString());
            } else {
                statusObject.put(PROPERTY_HTTP_ERROR_STATUS, responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        return statusObject;
    }

    public synchronized void triggerScene(String sceneId, String trigger) {
        JSONObject statusObject = new JSONObject();
        String url = String.format(SCENE_URL, gateway.getIpAddress(), sceneId) + "/" + trigger;
        try {
            Request homeRequest = httpClient.POST(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus != 200) {
                logger.warn("DIRIGERA Scene trigger failed with  {}", responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
    }

    public int sendPatch(String id, JSONObject attributes) {
        String url = String.format(DEVICE_URL, gateway.getIpAddress(), id);
        // pack attributes into correct send data
        JSONObject data = new JSONObject();
        data.put(Model.ATTRIBUTES, attributes);
        JSONArray dataArray = new JSONArray();
        dataArray.put(data);
        StringContentProvider stringProvider = new StringContentProvider("application/json", dataArray.toString(),
                StandardCharsets.UTF_8);
        logger.info("DIRIGERA API send {} to {}", dataArray, url);
        Request deviceRequest = httpClient.newRequest(url).method("PATCH")
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(stringProvider);

        try {
            ContentResponse response = addAuthorizationHeader(deviceRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200 || responseStatus == 202) {
                logger.info("DIRIGERA API send {} to {} delivered", dataArray, url);
            } else {
                logger.info("DIRIGERA API send {} to {} failed with status {}", dataArray, url, response.getStatus());
            }
            return responseStatus;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA API call to {} failed {}", url, e.getMessage());
            return 500;
        }
    }

    public synchronized Map<String, ?> getImage(String imageURL) {
        try {
            ContentResponse response = httpClient.GET(imageURL);
            if (response.getStatus() == 200) {
                logger.info("DIRIGERA API Image call {} delivers {} {}", imageURL, response.getMediaType(),
                        response.getContent() != null);
                String mimeType = response.getMediaType();
                if (mimeType == null) {
                    mimeType = RawType.DEFAULT_MIME_TYPE;
                }
                return Map.of("image", response.getContent(), "mimeType", mimeType);
            } else {
                logger.warn("DIRIGERA API call to {} failed {}", imageURL, response.getStatus());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("DIRIGERA API call to {} failed {}", imageURL, e.getMessage());
        }
        return Map.of("image", new byte[] {}, "mimeType", "");
    }
}
