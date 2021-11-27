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
package org.openhab.binding.renault.internal.renault.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.openhab.binding.renault.internal.renault.api.exceptions.RenaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * This is a Java version of the python renault-api project developed here:
 * https://github.com/hacf-fr/renault-api
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class MyRenaultHttpSession {

    private Car car = new Car();
    private RenaultConfiguration config;
    private HttpClient httpClient;
    private @Nullable Constants constants;
    private @Nullable String kamereonToken;
    private @Nullable String kamereonaccountId;
    private @Nullable String cookieValue;
    private @Nullable String personId;
    private @Nullable String gigyaDataCenter;
    private @Nullable String jwt;

    private final Logger logger = LoggerFactory.getLogger(MyRenaultHttpSession.class);

    public MyRenaultHttpSession(RenaultConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    public void initSesssion() throws Exception {
        this.constants = new Constants(config.locale);
        httpClient.start();
        login();
        getAccountInfo();
        getJWT();
        getAccountID();
        getVehicle();
    }

    public void updateCarData() {
        getCockpit();
        getBatteryStatus();
        getLocation();
        getHvacStatus();
    }

    private void login() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("loginID", config.myRenaultUsername);
        fields.add("password", config.myRenaultPassword);
        logger.debug("URL: {}/accounts.login", this.constants.getGigyaRootUrl());
        ContentResponse response = httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.login", fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            JsonObject sessionInfoJson = responseJson.getAsJsonObject("sessionInfo");
            cookieValue = sessionInfoJson.get("cookieValue").getAsString();
            logger.debug("Cookie: {}", cookieValue);
        } else {
            logger.warn("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException("Login Error: " + response.getReason());
        }
    }

    private void getAccountInfo() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("login_token", cookieValue);
        ContentResponse response = httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.getAccountInfo",
                fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            JsonObject dataJson = responseJson.getAsJsonObject("data");
            personId = dataJson.get("personId").getAsString();
            gigyaDataCenter = dataJson.get("gigyaDataCenter").getAsString();
            logger.debug("personId ID: {} gigyaDataCenter: {}", personId, gigyaDataCenter);
        } else {
            logger.warn("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException("Get Account Info Error: " + response.getReason());
        }
    }

    private void getJWT() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("login_token", cookieValue);
        fields.add("fields", "data.personId,data.gigyaDataCenter");
        fields.add("personId", personId);
        fields.add("gigyaDataCenter", gigyaDataCenter);
        ContentResponse response = this.httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.getJWT", fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            jwt = responseJson.get("id_token").getAsString();
            logger.debug("jwt: {} ", jwt);
        } else {
            logger.warn("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException("Get JWT Error: " + response.getReason());
        }
    }

    private void getAccountID() throws RenaultException {
        JsonObject responseJson = getKamereonResponse(
                "/commerce/v1/persons/" + personId + "?country=" + getCountry(config));
        if (responseJson != null) {
            JsonArray accounts = responseJson.getAsJsonArray("accounts");
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getAsJsonObject().get("accountType").getAsString().equals("MYRENAULT")) {
                    kamereonaccountId = accounts.get(i).getAsJsonObject().get("accountId").getAsString();
                    break;
                }
            }
        }
        if (kamereonaccountId == null) {
            throw new RenaultException("Can not get Kamereon MyRenault Account ID!");
        }
    }

    private void getVehicle() {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId + "/vehicles/"
                + config.vin + "/details?country=" + getCountry(config));
        if (responseJson != null) {
            car.setDetails(responseJson);
        }
    }

    private void getBatteryStatus() {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/battery-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setBatteryStatus(responseJson);
        }
    }

    private void getHvacStatus() {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/hvac-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setHVACStatus(responseJson);
        }
    }

    private void getCockpit() {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/cockpit?country=" + getCountry(config));
        if (responseJson != null) {
            car.setCockpit(responseJson);
        }
    }

    private void getLocation() {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/location?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLocation(responseJson);
        }
    }

    private @Nullable JsonObject getKamereonResponse(String path) {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(HttpMethod.GET)
                .header("Content-type", "application/vnd.api+json").header("apikey", this.constants.getKamereonApiKey())
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt);
        try {
            ContentResponse response = request.send();
            if (HttpStatus.OK_200 == response.getStatus()) {
                logger.debug("Kamereon Response: {}", response.getContentAsString());
                return JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            } else {
                logger.warn("Kamereon Response: [{}] {} {}", response.getStatus(), response.getReason(),
                        response.getContentAsString());
            }
        } catch (JsonParseException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
        }
        return null;
    }

    private String getCountry(RenaultConfiguration config) {
        String country = "XX";
        if (config.locale.length() == 5) {
            country = config.locale.substring(3);
        }
        return country;
    }

    public Car getCar() {
        return car;
    }
}
