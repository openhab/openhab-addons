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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Translation of this python code: https://github.com/hacf-fr/renault-api
 *
 * @author Doug Culnane - Initial contribution
 */
public class MyRenaultHttpSession {

    private HttpClient httpClient;
    private Constants constants;

    private Car car;
    private String kamereonToken;
    private String kamereonaccountId;
    private String cookieValue;
    private String personId;
    private String gigyaDataCenter;
    private String jwt;

    private final Logger logger = LoggerFactory.getLogger(MyRenaultHttpSession.class);

    public MyRenaultHttpSession(RenaultConfiguration config) throws Exception {

        car = new Car();
        this.httpClient = new HttpClient(new SslContextFactory(true));
        this.constants = new Constants(config.locale);

        httpClient.start();
        login(config);
        getAccountInfo(config);
        getJWT(config);
        getAccountID(config);
        getVehicle(config);
    }

    public void updateCarData(RenaultConfiguration config) throws Exception {
        getCockpit(config);
        getBatteryStatus(config);
        getLocation(config);
        getHvacStatus(config);
    }

    private void login(RenaultConfiguration config) throws Exception {

        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("loginID", config.myRenaultUsername);
        fields.add("password", config.myRenaultPassword);

        logger.debug("URL: {}/accounts.login", this.constants.getGigyaRootUrl());
        ContentResponse response = this.httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.login", fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            JsonObject sessionInfoJson = responseJson.getAsJsonObject("sessionInfo");
            cookieValue = sessionInfoJson.get("cookieValue").getAsString();
            logger.debug("Cookie: {}", cookieValue);
        } else {
            logger.error("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new Exception("Login Error: " + response.getReason());
        }
    }

    private void getAccountInfo(RenaultConfiguration config) throws Exception {

        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("login_token", cookieValue);

        ContentResponse response = this.httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.getAccountInfo",
                fields);

        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            JsonObject dataJson = responseJson.getAsJsonObject("data");
            personId = dataJson.get("personId").getAsString();
            gigyaDataCenter = dataJson.get("gigyaDataCenter").getAsString();
            logger.debug("personId ID: {} gigyaDataCenter: {}", personId, gigyaDataCenter);
        } else {
            logger.error("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new Exception("Get Account Info Error: " + response.getReason());
        }
    }

    private void getJWT(RenaultConfiguration config) throws Exception {

        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("login_token", cookieValue);
        fields.add("fields", "data.personId,data.gigyaDataCenter");
        fields.add("personId", personId);
        fields.add("gigyaDataCenter", gigyaDataCenter);

        ContentResponse response = this.httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.getJWT", fields);

        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            jwt = responseJson.get("id_token").getAsString();
            logger.debug("jwt: {} ", jwt);
        } else {
            logger.error("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new Exception("Get JWT Error: " + response.getReason());
        }
    }

    private void getAccountID(RenaultConfiguration config) throws Exception {

        Request request = this.httpClient.newRequest(this.constants.getKamereonRootUrl() + "/commerce/v1/persons/"
                + personId + "?country=" + getCountry(config));
        request.method(HttpMethod.GET);
        request.getHeaders().put(new HttpField("Content-type", "application/vnd.api+json"));
        request.getHeaders().put(new HttpField("apikey", this.constants.getKamereonApiKey()));
        request.getHeaders().put(new HttpField("x-gigya-id_token", jwt));
        logger.debug("Kamereon Request: {}", request.getURI().toString());

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            JsonArray accounts = responseJson.getAsJsonArray("accounts");
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getAsJsonObject().get("accountType").getAsString().equals("MYRENAULT")) {
                    kamereonaccountId = accounts.get(i).getAsJsonObject().get("accountId").getAsString();
                }
            }
            logger.debug("kamereonaccountId: {} ", kamereonaccountId);
        } else {
            logger.error("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new Exception("Get Account ID Error: " + response.getReason());
        }
    }

    private void getVehicle(RenaultConfiguration config) throws Exception {

        Request request = getKamereonRequest("/commerce/v1/accounts/" + kamereonaccountId + "/vehicles/" + config.vin
                + "/details?country=" + getCountry(config));

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            logger.debug("responseJson: {} ", responseJson.toString());
            car.setDetails(responseJson);
        } else {
            logger.warn("Vehicle Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
        }
    }

    private void getBatteryStatus(RenaultConfiguration config) throws Exception {

        Request request = getKamereonRequest("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/battery-status?country=" + getCountry(config));

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            logger.debug("responseJson: {} ", responseJson.toString());
            car.setBatteryStatus(responseJson);
        } else {
            logger.warn("BatteryStatus Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
        }
    }

    private void getHvacStatus(RenaultConfiguration config) throws Exception {

        Request request = getKamereonRequest("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/hvac-status?country=" + getCountry(config));

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            logger.debug("responseJson: {} ", responseJson.toString());
            car.setHVACStatus(responseJson);
        } else {
            logger.warn("HvacStatus Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
        }
    }

    private void getCockpit(RenaultConfiguration config) throws Exception {

        Request request = getKamereonRequest("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/cockpit?country=" + getCountry(config));

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            logger.debug("responseJson: {} ", responseJson.toString());
            car.setCockpit(responseJson);
        } else {
            logger.warn("Cockpit Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
        }
    }

    private void getLocation(RenaultConfiguration config) throws Exception {

        Request request = getKamereonRequest("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/location?country=" + getCountry(config));

        ContentResponse response = request.send();
        if (HttpStatus.OK_200 == response.getStatus()) {
            JsonObject responseJson = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();
            logger.debug("responseJson: {} ", responseJson.toString());
            car.setLocation(responseJson);
        } else {
            logger.warn("Location Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
        }
    }

    private Request getKamereonRequest(String path) {
        Request request = this.httpClient.newRequest(this.constants.getKamereonRootUrl() + path);
        request.method(HttpMethod.GET);
        request.getHeaders().put(new HttpField("Content-type", "application/vnd.api+json"));
        request.getHeaders().put(new HttpField("apikey", this.constants.getKamereonApiKey()));
        request.getHeaders().put(new HttpField("x-kamereon-authorization", "Bearer " + kamereonToken));
        request.getHeaders().put(new HttpField("x-gigya-id_token", jwt));
        logger.debug("Kamereon Request: {}", request.getURI().toString());
        return request;
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
