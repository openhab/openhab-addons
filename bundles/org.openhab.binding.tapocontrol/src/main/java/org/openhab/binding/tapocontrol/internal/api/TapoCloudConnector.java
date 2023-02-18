/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.api;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorConstants.*;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO-Cloud connections.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoCloudConnector {
    private final Logger logger = LoggerFactory.getLogger(TapoCloudConnector.class);
    private final TapoBridgeHandler bridge;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    private String token = "";
    private String url = TAPO_CLOUD_URL;
    private String uid;

    /**
     * INIT CLASS
     * 
     */
    public TapoCloudConnector(TapoBridgeHandler bridge, HttpClient httpClient) {
        this.bridge = bridge;
        this.httpClient = httpClient;
        this.uid = bridge.getUID().getAsString();
    }

    /**
     * handle error
     * 
     * @param tapoError TapoErrorHandler
     */
    protected void handleError(TapoErrorHandler tapoError) {
        this.bridge.setError(tapoError);
    }

    /***********************************
     *
     * HTTP (Cloud)-Actions
     *
     ************************************/

    /**
     * LOGIN TO CLOUD (get Token)
     * 
     * @param username unencrypted username
     * @param password unencrypted password
     * @return true if login was successfull
     */
    public Boolean login(String username, String password) {
        this.token = getToken(username, password, UUID.randomUUID().toString());
        this.url = TAPO_CLOUD_URL + "?token=" + token;
        return !this.token.isBlank();
    }

    /**
     * logout
     */
    public void logout() {
        this.token = "";
    }

    /**
     * GET TOKEN FROM TAPO-CLOUD
     * 
     * @param email
     * @param password
     * @param terminalUUID
     * @return
     */
    private String getToken(String email, String password, String terminalUUID) {
        String token = "";

        /* create login payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "login";
        plBuilder.addParameter("appType", TAPO_APP_TYPE);
        plBuilder.addParameter("cloudUserName", email);
        plBuilder.addParameter("cloudPassword", password);
        plBuilder.addParameter("terminalUUID", terminalUUID);
        String payload = plBuilder.getPayload();

        ContentResponse response = sendCloudRequest(TAPO_CLOUD_URL, payload);
        if (response != null) {
            token = getTokenFromResponse(response);
        }
        return token;
    }

    private String getTokenFromResponse(ContentResponse response) {
        /* work with response */
        if (response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
            if (jsonObject != null) {
                Integer errorCode = jsonObject.get("error_code").getAsInt();
                if (errorCode == 0) {
                    token = jsonObject.getAsJsonObject("result").get("token").getAsString();
                } else {
                    /* return errorcode from device */
                    String msg = jsonObject.get("msg").getAsString();
                    handleError(new TapoErrorHandler(errorCode, msg));
                    logger.trace("cloud returns error: '{}'", rBody);
                }
            } else {
                handleError(new TapoErrorHandler(ERR_JSON_DECODE_FAIL));
                logger.trace("unexpected json-response '{}'", rBody);
            }
        } else {
            handleError(new TapoErrorHandler(ERR_HTTP_RESPONSE, ERR_HTTP_RESPONSE_MSG));
            logger.warn("invalid response while login");
            token = "";
        }
        return token;
    }

    /**
     * 
     * @return JsonArray with deviceList
     */
    public JsonArray getDeviceList() {
        /* create payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "getDeviceList";
        String payload = plBuilder.getPayload();

        ContentResponse response = sendCloudRequest(this.url, payload);
        if (response != null) {
            return getDeviceListFromResponse(response);
        }
        return new JsonArray();
    }

    /**
     * get DeviceList from Contenresponse
     * 
     * @param response
     * @return
     */
    private JsonArray getDeviceListFromResponse(ContentResponse response) {
        /* work with response */
        if (response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
            if (jsonObject != null) {
                /* get errocode (0=success) */
                Integer errorCode = jsonObject.get("error_code").getAsInt();
                if (errorCode == 0) {
                    JsonObject result = jsonObject.getAsJsonObject("result");
                    return result.getAsJsonArray("deviceList");
                } else {
                    /* return errorcode from device */
                    handleError(new TapoErrorHandler(errorCode, "device answers with errorcode"));
                    logger.trace("cloud returns error: '{}'", rBody);
                }
            } else {
                logger.trace("enexpected json-response '{}'", rBody);
            }
        } else {
            logger.trace("response error '{}'", response.getContentAsString());
        }
        return new JsonArray();
    }

    /***********************************
     *
     * HTTP-ACTIONS
     *
     ************************************/
    /**
     * SEND SYNCHRON HTTP-REQUEST
     * 
     * @param url url request is sent to
     * @param payload payload (String) to send
     * @return ContentResponse of request
     */
    @Nullable
    protected ContentResponse sendCloudRequest(String url, String payload) {
        Request httpRequest = httpClient.newRequest(url).method(HttpMethod.POST.toString());

        /* set header */
        httpRequest.header("content-type", CONTENT_TYPE_JSON);
        httpRequest.header("Accept", CONTENT_TYPE_JSON);

        /* add request body */
        httpRequest.content(new StringContentProvider(payload, CONTENT_CHARSET), CONTENT_TYPE_JSON);

        try {
            ContentResponse httpResponse = httpRequest.send();
            return httpResponse;
        } catch (InterruptedException e) {
            logger.debug("({}) sending request interrupted: {}", uid, e.toString());
            handleError(new TapoErrorHandler(e));
        } catch (TimeoutException e) {
            logger.debug("({}) sending request timeout: {}", uid, e.toString());
            handleError(new TapoErrorHandler(ERR_CONNECT_TIMEOUT, e.toString()));
        } catch (Exception e) {
            logger.debug("({}) sending request failed: {}", uid, e.toString());
            handleError(new TapoErrorHandler(e));
        }
        return null;
    }
}
