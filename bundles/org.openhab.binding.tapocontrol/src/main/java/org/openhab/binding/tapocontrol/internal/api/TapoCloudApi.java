/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device cloud-connections.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoCloudApi {
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(TapoCloudApi.class);
    private final TapoHttp tapoHttp;
    private String token = "";

    /**
     * INIT CLASS
     * 
     */
    public TapoCloudApi() {
        this.tapoHttp = new TapoHttp();
        this.tapoHttp.setSSL(true);
    }

    /**
     * LOGIN TO CLOUD (get Token)
     * 
     * @param username unencrypted username
     * @param password unencrypted password
     * @return true if login was successfull
     */
    public Boolean login(String username, String password) {
        this.token = getToken(username, password, TAPO_TERMINAL_UUID);
        return this.token != "";
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

        tapoHttp.url = TAPO_CLOUD_URL;
        tapoHttp.request = payload;
        TapoHttpResponse response = tapoHttp.send();

        /* work with response */
        if (response.responseIsOK()) {
            String rBody = response.getResponseBody();
            JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
            try {
                token = jsonObject.getAsJsonObject("result").get("token").getAsString();
            } catch (Exception e) {
                logger.trace("enexpected json-response '{}'", rBody);
            }
        } else {
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

        tapoHttp.url = TAPO_CLOUD_URL + "?token=" + token;
        tapoHttp.request = payload;
        TapoHttpResponse response = tapoHttp.send();

        /* work with response */
        if (response.responseIsOK()) {
            String rBody = response.getResponseBody();
            try {
                JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
                /* get errocode (0=success) */
                Integer errorCode = jsonObject.get("error_code").getAsInt();
                if (errorCode == 0) {
                    JsonObject result = jsonObject.getAsJsonObject("result");
                    return result.getAsJsonArray("deviceList");
                } else {
                    /* return errorcode from device */
                    // tapoError.raiseError(errorCode, "device answers with errorcode");
                    logger.trace("device answers with errorcode '{}'", rBody);
                }
            } catch (Exception e) {
                logger.trace("enexpected json-response '{}'", rBody);
            }
        } else {
            logger.trace("response error '{}'", response.getResponseBody());
        }
        return new JsonArray();
    }
}
