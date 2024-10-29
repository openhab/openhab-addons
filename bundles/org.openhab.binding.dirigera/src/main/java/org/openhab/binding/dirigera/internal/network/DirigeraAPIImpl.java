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
import org.openhab.binding.dirigera.internal.interfaces.DirigeraAPI;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.RawType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DirigeraAPIImpl} provides easy access towards REST API
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class DirigeraAPIImpl implements DirigeraAPI {
    private final Logger logger = LoggerFactory.getLogger(DirigeraAPIImpl.class);

    /**
     * Parameters
     * 1) UUID of this scene
     * 2) Name of this scene
     * 3) click pattern
     * 4) button index
     * 5) controller id
     */
    private String scenePattern = "{\"id\": \"%s\",\"type\": \"customScene\",\"info\": {\"name\": \"%s\",\"icon\": \"scenes_home_filled\"},\"triggers\": [{\"type\": \"controller\",\"trigger\": {\"controllerType\": \"shortcutController\",\"clickPattern\": \"%s\",\"buttonIndex\": %s,\"deviceId\": \"%s\"}}],\"actions\": [],\"commands\": [],\"undoAllowedDuration\": 30}";
    private HttpClient httpClient;
    private Gateway gateway;

    private boolean calling = false;

    public DirigeraAPIImpl(HttpClient httpClient, Gateway gateway) {
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

    @Override
    public JSONObject readHome() {
        String url = String.format(HOME_URL, gateway.getIpAddress());
        startCalling(url);
        JSONObject statusObject = new JSONObject();
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200) {
                statusObject = new JSONObject(response.getContentAsString());
            } else {
                statusObject.put(PROPERTY_HTTP_ERROR_STATUS, responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        endCalling(url);
        return statusObject;
    }

    @Override
    public JSONObject readDevice(String deviceId) {
        String url = String.format(DEVICE_URL, gateway.getIpAddress(), deviceId);
        startCalling(url);
        JSONObject statusObject = new JSONObject();
        try {
            Request homeRequest = httpClient.newRequest(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200) {
                statusObject = new JSONObject(response.getContentAsString());
            } else {
                statusObject.put(PROPERTY_HTTP_ERROR_STATUS, responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        endCalling(url);
        return statusObject;
    }

    @Override
    public void triggerScene(String sceneId, String trigger) {
        String url = String.format(SCENE_URL, gateway.getIpAddress(), sceneId) + "/" + trigger;
        startCalling(url);
        JSONObject statusObject = new JSONObject();
        try {
            Request homeRequest = httpClient.POST(url);
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus != 200 && responseStatus != 202) {
                logger.warn("DIRIGERA Scene trigger failed with {}", responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling {}", url);
        }
        endCalling(url);
    }

    @Override
    public int sendPatch(String id, JSONObject attributes) {
        String url = String.format(DEVICE_URL, gateway.getIpAddress(), id);
        // pack attributes into data json and then into an array
        JSONObject data = new JSONObject();
        data.put(Model.ATTRIBUTES, attributes);
        JSONArray dataArray = new JSONArray();
        dataArray.put(data);
        StringContentProvider stringProvider = new StringContentProvider("application/json", dataArray.toString(),
                StandardCharsets.UTF_8);
        logger.info("DIRIGERA API send {} to {}", dataArray, url);
        Request deviceRequest = httpClient.newRequest(url).method("PATCH")
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(stringProvider);

        startCalling(url);
        int responseStatus = 500;
        try {
            ContentResponse response = addAuthorizationHeader(deviceRequest).timeout(10, TimeUnit.SECONDS).send();
            responseStatus = response.getStatus();
            if (responseStatus == 200 || responseStatus == 202) {
                logger.debug("DIRIGERA API send {} to {} delivered", dataArray, url);
            } else {
                logger.info("DIRIGERA API send {} to {} failed with status {}", dataArray, url, response.getStatus());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA API call to {} failed {}", url, e.getMessage());
        }
        endCalling(url);
        return responseStatus;
    }

    @Override
    public State getImage(String imageURL) {
        State image = UnDefType.UNDEF;
        startCalling(imageURL);
        try {
            ContentResponse response = httpClient.GET(imageURL);
            if (response.getStatus() == 200) {
                String mimeType = response.getMediaType();
                if (mimeType == null) {
                    mimeType = RawType.DEFAULT_MIME_TYPE;
                }
                image = new RawType(response.getContent(), mimeType);
            } else {
                logger.warn("DIRIGERA API call to {} failed {}", imageURL, response.getStatus());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("DIRIGERA API call to {} failed {}", imageURL, e.getMessage());
        }
        endCalling(imageURL);
        return image;
    }

    @Override
    public JSONObject readScene(String sceneId) {
        String url = String.format(SCENE_URL, gateway.getIpAddress(), sceneId);
        JSONObject statusObject = new JSONObject();
        Request homeRequest = httpClient.newRequest(url);
        startCalling(url);
        try {
            ContentResponse response = addAuthorizationHeader(homeRequest).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus == 200) {
                statusObject = new JSONObject(response.getContentAsString());
            } else {
                statusObject.put(PROPERTY_HTTP_ERROR_STATUS, responseStatus);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            statusObject.put(PROPERTY_HTTP_ERROR_STATUS, e.getMessage());
            logger.warn("DIRIGERA Exception calling  {}", url);
        }
        endCalling(url);
        return statusObject;
    }

    @Override
    public String createScene(String uuid, String clickPattern, String controllerId) {
        String url = String.format(SCENES_URL, gateway.getIpAddress());
        String payload = String.format(scenePattern, uuid, "openHAB Shortcut Proxy", clickPattern, "0", controllerId);
        StringContentProvider stringProvider = new StringContentProvider("application/json", payload,
                StandardCharsets.UTF_8);
        logger.info("DIRIGERA API send {} to {}", payload, url);
        Request sceneCreateRequest = httpClient.newRequest(url).method("POST")
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(stringProvider);

        startCalling(url);
        int responseStatus = 500;
        String responseUUID = "";
        int retryCounter = 3;
        while (retryCounter > 0 && !uuid.equals(responseUUID)) {
            try {
                ContentResponse response = addAuthorizationHeader(sceneCreateRequest).timeout(10, TimeUnit.SECONDS)
                        .send();
                responseStatus = response.getStatus();
                if (responseStatus == 200 || responseStatus == 202) {
                    logger.info("DIRIGERA API send {} to {} delivered", payload, url);
                    String responseString = response.getContentAsString();
                    JSONObject responseJSON = new JSONObject(responseString);
                    responseUUID = responseJSON.getString(PROPERTY_DEVICE_ID);
                } else {
                    logger.info("DIRIGERA API send {} to {} failed with status {}", payload, url, response.getStatus());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("DIRIGERA API call to {} failed {}", url, e.getMessage());
            }
            logger.info("DIRIGERA API createScene failed {} retries remaining", retryCounter);
            retryCounter--;
        }
        endCalling(url);
        return responseUUID;
    }

    @Override
    public void deleteScene(String uuid) {
        String url = String.format(SCENES_URL, gateway.getIpAddress()) + "/" + uuid;
        Request sceneDeleteRequest = httpClient.newRequest(url).method("DELETE");
        startCalling(url);
        int responseStatus = 500;
        int retryCounter = 3;
        while (retryCounter > 0 && responseStatus != 200 && responseStatus != 202) {
            try {
                ContentResponse response = addAuthorizationHeader(sceneDeleteRequest).timeout(10, TimeUnit.SECONDS)
                        .send();
                responseStatus = response.getStatus();
                if (responseStatus == 200 || responseStatus == 202) {
                    logger.debug("DIRIGERA API delete {} performed", url);
                } else {
                    logger.debug("DIRIGERA API send {} failed with status {}", url, response.getStatus());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("DIRIGERA API call to {} failed {}", url, e.getMessage());
            }
            logger.info("DIRIGERA API deleteScene failed with status {}, {} retries remaining", responseStatus,
                    retryCounter);
            retryCounter--;
        }
        endCalling(url);
    }

    private void startCalling(String request) {
        synchronized (this) {
            while (calling) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            calling = true;
        }
    }

    private void endCalling(String request) {
        synchronized (this) {
            calling = false;
            this.notifyAll();
        }
    }
}
