/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.openhab.binding.renault.internal.api.Car.ChargingMode;
import org.openhab.binding.renault.internal.api.exceptions.RenaultException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultForbiddenException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultNotImplementedException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    private static final String CHARGING_MODE_SCHEDULE = "schedule_mode";
    private static final String CHARGING_MODE_ALWAYS = "always_charging";

    private RenaultConfiguration config;
    private HttpClient httpClient;
    private Constants constants;
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
        this.constants = new Constants(config.locale);
    }

    public void initSesssion(Car car) throws RenaultException, RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, InterruptedException, ExecutionException, TimeoutException {
        login();
        getAccountInfo();
        getJWT();
        getAccountID();

        final String imageURL = car.getImageURL();
        if (imageURL == null) {
            getVehicle(car);
        }
    }

    private void login() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("loginID", config.myRenaultUsername);
        fields.add("password", config.myRenaultPassword);
        logger.debug("URL: {}/accounts.login", this.constants.getGigyaRootUrl());
        ContentResponse response = httpClient.FORM(this.constants.getGigyaRootUrl() + "/accounts.login", fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            try {
                JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                JsonObject sessionInfoJson = responseJson.getAsJsonObject("sessionInfo");
                if (sessionInfoJson != null) {
                    JsonElement element = sessionInfoJson.get("cookieValue");
                    if (element != null) {
                        cookieValue = element.getAsString();
                        logger.debug("Cookie: {}", cookieValue);
                    }
                }
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException("Login Error: cookie value not found in JSON response");
            }
            if (cookieValue == null) {
                logger.warn("Login Error: cookie value not found! Response: [{}] {}\n{}", response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
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
            try {
                JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                JsonObject dataJson = responseJson.getAsJsonObject("data");
                if (dataJson != null) {
                    JsonElement element1 = dataJson.get("personId");
                    JsonElement element2 = dataJson.get("gigyaDataCenter");
                    if (element1 != null && element2 != null) {
                        personId = element1.getAsString();
                        gigyaDataCenter = element2.getAsString();
                        logger.debug("personId ID: {} gigyaDataCenter: {}", personId, gigyaDataCenter);
                    }
                }
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException(
                        "Get Account Info Error: personId or gigyaDataCenter value not found in JSON response");
            }
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
            try {
                JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                JsonElement element = responseJson.get("id_token");
                if (element != null) {
                    jwt = element.getAsString();
                    logger.debug("jwt: {} ", jwt);
                }
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException("Get JWT Error: jwt value not found in JSON response");
            }
        } else {
            logger.warn("Response: [{}] {}\n{}", response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException("Get JWT Error: " + response.getReason());
        }
    }

    private void getAccountID()
            throws RenaultException, RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
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

    public void getVehicle(Car car)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId + "/vehicles/"
                + config.vin + "/details?country=" + getCountry(config));
        if (responseJson != null) {
            car.setDetails(responseJson);
        }
    }

    public void getBatteryStatus(Car car)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/battery-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setBatteryStatus(responseJson);
        }
    }

    public void getHvacStatus(Car car)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/hvac-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setHVACStatus(responseJson);
        }
    }

    public void getCockpit(Car car)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/cockpit?country=" + getCountry(config));
        if (responseJson != null) {
            car.setCockpit(responseJson);
        }
    }

    public void getLocation(Car car)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/location?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLocation(responseJson);
        }
    }

    public void actionHvacOn(double hvacTargetTemperature)
            throws RenaultForbiddenException, RenaultNotImplementedException {
        Request request = httpClient
                .newRequest(this.constants.getKamereonRootUrl() + "/commerce/v1/accounts/" + kamereonaccountId
                        + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/actions/hvac-start?country="
                        + getCountry(config))
                .method(HttpMethod.POST).header("Content-type", "application/vnd.api+json")
                .header("apikey", this.config.kamereonApiKey)
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt);
        request.content(new StringContentProvider(
                "{\"data\":{\"type\":\"HvacStart\",\"attributes\":{\"action\":\"start\",\"targetTemperature\":\""
                        + hvacTargetTemperature + "\"}}}",
                "utf-8"));
        try {
            ContentResponse response = request.send();
            logger.debug("Kamereon Response HVAC ON: {}", response.getContentAsString());
            if (HttpStatus.OK_200 != response.getStatus()) {
                logger.warn("Kamereon Response: [{}] {} {}", response.getStatus(), response.getReason(),
                        response.getContentAsString());
                if (HttpStatus.FORBIDDEN_403 == response.getStatus()) {
                    throw new RenaultForbiddenException(
                            "Kamereon Response Forbidden! Ensure the car is paired in your MyRenault App.");
                } else if (HttpStatus.NOT_IMPLEMENTED_501 == response.getStatus()) {
                    throw new RenaultNotImplementedException(
                            "Kamereon Service Not Implemented: [" + response.getStatus() + "] " + response.getReason());
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
        }
    }

    public void actionChargeMode(ChargingMode mode) throws RenaultForbiddenException, RenaultNotImplementedException {
        Request request = httpClient
                .newRequest(this.constants.getKamereonRootUrl() + "/commerce/v1/accounts/" + kamereonaccountId
                        + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/actions/charge-mode?country="
                        + getCountry(config))
                .method(HttpMethod.POST).header("Content-type", "application/vnd.api+json")
                .header("apikey", this.config.kamereonApiKey)
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt);

        final String apiMode = ChargingMode.SCHEDULE_MODE.equals(mode) ? CHARGING_MODE_SCHEDULE : CHARGING_MODE_ALWAYS;
        request.content(new StringContentProvider(
                "{\"data\":{\"type\":\"ChargeMode\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}", "utf-8"));
        try {
            ContentResponse response = request.send();
            logger.debug("Kamereon Response set ChargeMode: {}", response.getContentAsString());
            if (HttpStatus.OK_200 != response.getStatus()) {
                logger.warn("Kamereon Response: [{}] {} {}", response.getStatus(), response.getReason(),
                        response.getContentAsString());
                if (HttpStatus.FORBIDDEN_403 == response.getStatus()) {
                    throw new RenaultForbiddenException(
                            "Kamereon Response Forbidden! Ensure the car is paired in your MyRenault App.");
                } else if (HttpStatus.NOT_IMPLEMENTED_501 == response.getStatus()) {
                    throw new RenaultNotImplementedException(
                            "Kamereon Service Not Implemented: [" + response.getStatus() + "] " + response.getReason());
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
        }
    }

    private @Nullable JsonObject getKamereonResponse(String path)
            throws RenaultForbiddenException, RenaultUpdateException, RenaultNotImplementedException {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(HttpMethod.GET)
                .header("Content-type", "application/vnd.api+json").header("apikey", this.config.kamereonApiKey)
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt);
        try {
            ContentResponse response = request.send();
            if (HttpStatus.OK_200 == response.getStatus()) {
                logger.debug("Kamereon Response: {}", response.getContentAsString());
                return JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            } else {
                logger.warn("Kamereon Response: [{}] {} {}", response.getStatus(), response.getReason(),
                        response.getContentAsString());
                if (HttpStatus.FORBIDDEN_403 == response.getStatus()) {
                    throw new RenaultForbiddenException(
                            "Kamereon Response Forbidden! Ensure the car is paired in your MyRenault App.");
                } else if (HttpStatus.NOT_IMPLEMENTED_501 == response.getStatus()) {
                    throw new RenaultNotImplementedException(
                            "Kamereon Service Not Implemented: [" + response.getStatus() + "] " + response.getReason());
                } else {
                    throw new RenaultUpdateException(
                            "Kamereon Response Failed! Error: [" + response.getStatus() + "] " + response.getReason());
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
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
}
