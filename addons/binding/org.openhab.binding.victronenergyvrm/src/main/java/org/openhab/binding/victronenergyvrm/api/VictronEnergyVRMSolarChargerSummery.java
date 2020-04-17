/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Get data of solarcharger from vrm api
 *
 *
 * @author Samuel Lueckoff
 */
public class VictronEnergyVRMSolarChargerSummery {
    private final Logger logger = LoggerFactory.getLogger(VictronEnergyVRMSolarChargerSummery.class);
    private String baseUrl;
    private BigDecimal instId;
    private BigDecimal instance;
    private String ScV;
    private String ScS;
    private String YT;
    private String YY;
    private String ScW;
    private Boolean hasOldData;
    private String secondsAgo;
    private Boolean success;

    public VictronEnergyVRMSolarChargerSummery(String baseUrl, BigDecimal instId, BigDecimal instance) {
        // Constructor
        this.baseUrl = baseUrl;
        this.instId = instId;
        this.instance = instance;
        this.success = false;
        this.ScV = "0.0";
    }

    public void loadData(String token) {
        String strSuccess;
        String ScSUrl = baseUrl + "installations/" + instId.toString() + "/widgets/SolarChargerSummary?instance="
                + instance.toString();
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpJettyClient = new HttpClient(sslContextFactory);
        httpJettyClient.setFollowRedirects(false);

        try {

            httpJettyClient.start();
            Request request = httpJettyClient.newRequest(ScSUrl);
            request.method(HttpMethod.GET);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("X-Authorization", "Bearer " + token);
            logger.debug("Request: " + request.toString());

            ContentResponse response = request.send();
            String res = new String(response.getContent());

            logger.debug("StatusCode: " + response.getStatus());
            logger.debug("Reason: " + response.getReason());

            if (response.getStatus() != 200) {
                logger.warn("httpResponse: " + response.getReason());
            } else {
                logger.debug("httpResponse: " + response.getReason());
            }
            JsonParser jp = new JsonParser();
            JsonObject jo = (JsonObject) jp.parse(res);

            strSuccess = jo.get("success").toString();
            if (strSuccess.equals("true")) {
                success = true;
                JsonObject joData = jo.getAsJsonObject("records").getAsJsonObject("data");
                Set<Map.Entry<String, JsonElement>> entries = joData.entrySet();
                for (Map.Entry<String, JsonElement> entry : entries) {
                    if (entry.getKey().equals("hasOldData")) {
                        hasOldData = entry.getValue().getAsBoolean();
                        logger.debug("Has old Data?: " + hasOldData.toString());
                    } else if (entry.getKey().equals("secondsAgo")) {
                        JsonObject joEntry = joData.getAsJsonObject(entry.getKey());
                        secondsAgo = joEntry.get("value").toString();
                        // Entferne " am Anfang und am Ende
                        secondsAgo = secondsAgo.substring(1, secondsAgo.length() - 1);
                        logger.debug("Seconds ago: " + secondsAgo);
                    } else {
                        JsonObject joEntry = joData.getAsJsonObject(entry.getKey());
                        String entryCode = joEntry.get("code").toString();
                        // Entferne " am Anfang und Ende des token
                        entryCode = entryCode.substring(1, entryCode.length() - 1);
                        logger.debug("ScS json DATA Code: " + entryCode);
                        String value = "";
                        switch (entryCode) {
                            case "ScV":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.ScV = value;
                                logger.debug("ScS json ScV Value: " + value);
                                break;
                            case "ScS":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.ScS = value;
                                logger.debug("ScS json ScS Value: " + value);
                                break;
                            case "YT":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.YT = value;
                                logger.debug("ScS json YT Value: " + value);
                                break;
                            case "YY":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.YY = value;
                                logger.debug("ScS json YT Value: " + value);
                                break;
                            case "ScW":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.ScW = value;
                                logger.debug("ScS json YT Value: " + value);
                                break;
                        }
                    }
                }
                // Switch YY etc.
            } else {
                success = false;
            }
            logger.debug("scs load data state success?: " + success.toString() + "!");
            httpJettyClient.stop();

        } catch (IOException e) {
            logger.error(e.toString());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public Boolean getSuccess() {
        return success;
    }

    public String getScV() {
        return this.ScV;
    }

    public String getScS() {
        return this.ScS;
    }

    public String getScW() {
        return this.ScW;
    }

    public String getYT() {
        return this.YT;
    }

    public String getYY() {
        return this.YY;
    }

    public Boolean getHasOldData() {
        return this.hasOldData;
    }

    public String getSecondsAgo() {
        return this.secondsAgo;
    }

}
