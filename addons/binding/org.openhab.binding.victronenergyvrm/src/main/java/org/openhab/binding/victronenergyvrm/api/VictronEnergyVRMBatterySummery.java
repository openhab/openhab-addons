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
 * Get data of battery monitor from vrm api
 *
 *
 * @author Samuel Lueckoff
 */
public class VictronEnergyVRMBatterySummery {
    private final Logger logger = LoggerFactory.getLogger(VictronEnergyVRMSolarChargerSummery.class);
    private String baseUrl;
    private BigDecimal instId;
    private BigDecimal instance;
    private String BmV;
    private String BmVS;
    private String BmI;
    private String BmCE;
    private String BmSOC;
    private String BmTTG;
    private String BmAL;
    private String BmAH;
    private String BmALS;
    private String BmAHS;
    private String BmASoc;
    private String BmALT;
    private String BmAHT;
    private String BmAM;
    private String secondsAgo;
    private Boolean hasOldData;
    private Boolean success;

    public VictronEnergyVRMBatterySummery(String baseUrl, BigDecimal instId, BigDecimal instance) {
        // Constructor
        this.baseUrl = baseUrl;
        this.instId = instId;
        this.instance = instance;
        this.success = false;
        this.BmV = "0.0";
    }

    public void loadData(String token) {
        String strSuccess;
        String ScSUrl = baseUrl + "installations/" + instId.toString() + "/widgets/BatterySummary?instance="
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
                            case "V":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmV = value;
                                logger.debug("Bm json V Value: " + value);
                                break;
                            case "VS":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmVS = value;
                                logger.debug("Bm json VS Value: " + value);
                                break;
                            case "I":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmI = value;
                                logger.debug("Bm json I Value: " + value);
                                break;
                            case "CE":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmCE = value;
                                logger.debug("Bm json CE Value: " + value);
                                break;
                            case "SOC":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmSOC = value;
                                logger.debug("Bm json SOC Value: " + value);
                                break;
                            case "TTG":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmTTG = value;
                                logger.debug("Bm json TTG Value: " + value);
                                break;
                            case "AL":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmAL = value;
                                logger.debug("Bm json AL Value: " + value);
                                break;
                            case "AH":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmAH = value;
                                logger.debug("Bm json AH Value: " + value);
                                break;
                            case "ALS":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmALS = value;
                                logger.debug("Bm json ALS Value: " + value);
                                break;
                            case "AHS":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmAHS = value;
                                logger.debug("Bm json AHS Value: " + value);
                                break;
                            case "ASoc":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmASoc = value;
                                logger.debug("Bm json Asoc Value: " + value);
                                break;
                            case "ALT":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmALT = value;
                                logger.debug("Bm json ALT Value: " + value);
                                break;
                            case "AHT":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmAHT = value;
                                logger.debug("Bm json AHT Value: " + value);
                                break;
                            case "AM":
                                value = joEntry.get("value").toString();
                                // Entferne " am Anfang und Ende des token
                                value = value.substring(1, value.length() - 1);
                                this.BmAM = value;
                                logger.debug("Bm json AM Value: " + value);
                                break;
                        }
                    }
                }
                // Switch YY etc.
            } else {
                success = false;
            }
            logger.debug("bm load data state success?: " + success.toString() + "!");
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

    public String getBmV() {
        return this.BmV;
    }

    public String getBmVS() {
        return this.BmVS;
    }

    public String getBmI() {
        return this.BmI;
    }

    public String getBmCE() {
        return this.BmCE;
    }

    public String getBmSOC() {
        return this.BmSOC;
    }

    public String getBmTTG() {
        return this.BmTTG;
    }

    public String getBmAL() {
        return this.BmAL;
    }

    public String getBmAH() {
        return this.BmAH;
    }

    public String getBmALS() {
        return this.BmALS;
    }

    public String getBmAHS() {
        return this.BmAHS;
    }

    public String getBmASoc() {
        return this.BmASoc;
    }

    public String getBmALT() {
        return this.BmALT;
    }

    public String getBmAHT() {
        return this.BmAHT;
    }

    public String getBmAM() {
        return this.BmAM;
    }

    public String getsecondsAgo() {
        return this.secondsAgo;
    }

    public Boolean getHasOldData() {
        return this.hasOldData;
    }

}
