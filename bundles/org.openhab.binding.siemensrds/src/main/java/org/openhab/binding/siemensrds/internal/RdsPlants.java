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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * Interface to the Plants List of a particular User
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class RdsPlants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(RdsPlants.class);

    @SerializedName("items")
    private List<PlantInfo> plants;

    static class PlantInfo {

        @SerializedName("id")
        private String plantId;
        @SerializedName("isOnline")
        private Boolean online;

        public String getId() {
            return plantId;
        }

        public Boolean isOnline() {
            return online;
        }
    }

    /*
     * execute the HTTP GET on the server
     */
    private static String httpGetPlantListJson(String apiKey, String token) {
        String result = "";
        SslContextFactory sslCtx = new SslContextFactory();
        HttpClient https = new HttpClient(sslCtx);

        try {
            https.start();
            try {
                Request req = https.newRequest(URL_PLANTS);

                req.method(HttpMethod.GET);
                req.agent(VAL_USER_AGENT);
                req.header(HttpHeader.ACCEPT, VAL_ACCEPT);
                req.header(HDR_SUB_KEY, apiKey);
                req.header(HDR_AUTHORIZE, String.format(VAL_AUTHORIZE, token));

                ContentResponse resp = req.send();

                int responseCode = resp.getStatus();
                if (responseCode == HttpStatus.OK_200) {
                    result = resp.getContentAsString();
                } else {
                    LOGGER.debug("httpGetPlantListJson: http error={}", responseCode);
                }
            } finally {
                https.stop();
            }
        } catch (Exception e) {
            LOGGER.error("httpGetPlantListJson: exception={}", e.getMessage());
        }
        return result;
    }

    /*
     * public method: execute a GET on the cloud server, parse JSON, and create a
     * class that encapsulates the data
     */
    @Nullable
    public static RdsPlants create(String apiKey, String token) {
        try {
            String json = httpGetPlantListJson(apiKey, token);
            if (!json.equals("")) {
                Gson gson = new Gson();
                return gson.fromJson(json, RdsPlants.class);
            }
        } catch (Exception e) {
            LOGGER.debug("create: exception={}", e.getMessage());
        }
        return null;
    }

    /*
     * public method: return the plant list
     */
    public List<PlantInfo> getPlants() {
        return plants;
    }

}
