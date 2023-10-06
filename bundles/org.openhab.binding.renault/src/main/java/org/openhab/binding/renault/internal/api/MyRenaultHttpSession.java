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
import org.openhab.binding.renault.internal.api.exceptions.RenaultAPIGatewayException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultActionException;
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
        final String url = this.constants.getGigyaRootUrl() + "/accounts.login";
        ContentResponse response = httpClient.FORM(url, fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
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
                logger.warn("Login Error: cookie value not found! Response: {}", response.getContentAsString());
                throw new RenaultException("Login Error: cookie value not found in JSON response");
            }
        } else {
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException("Login Error: " + response.getReason());
        }
    }

    private void getAccountInfo() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", this.constants.getGigyaApiKey());
        fields.add("login_token", cookieValue);
        final String url = this.constants.getGigyaRootUrl() + "/accounts.getAccountInfo";
        ContentResponse response = httpClient.FORM(url, fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
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
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
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
        final String url = this.constants.getGigyaRootUrl() + "/accounts.getJWT";
        ContentResponse response = this.httpClient.FORM(url, fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
            try {
                JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                JsonElement element = responseJson.get("id_token");
                if (element != null) {
                    jwt = element.getAsString();
                    logger.debug("GigyaApi jwt: {} ", jwt);
                }
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException("Get JWT Error: jwt value not found in JSON response");
            }
        } else {
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
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
                if (accounts.get(i).getAsJsonObject().get("accountType").getAsString().equals(config.accountType)) {
                    kamereonaccountId = accounts.get(i).getAsJsonObject().get("accountId").getAsString();
                    break;
                }
            }
        }
        if (kamereonaccountId == null) {
            throw new RenaultException("Can not get Kamereon " + config.accountType + " Account ID!");
        }
    }

    public void getVehicle(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId + "/vehicles/"
                + config.vin + "/details?country=" + getCountry(config));
        if (responseJson != null) {
            car.setDetails(responseJson);
        }
    }

    public void getBatteryStatus(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/battery-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setBatteryStatus(responseJson);
        }
    }

    public void getHvacStatus(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/hvac-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setHVACStatus(responseJson);
        }
    }

    public void getCockpit(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v2/cars/" + config.vin + "/cockpit?country=" + getCountry(config));
        if (responseJson != null) {
            car.setCockpit(responseJson);
        }
    }

    public void getLocation(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/location?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLocation(responseJson);
        }
    }

    public void getLockStatus(Car car) throws RenaultForbiddenException, RenaultUpdateException,
            RenaultNotImplementedException, RenaultAPIGatewayException {
        JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId
                + "/kamereon/kca/car-adapter/v1/cars/" + config.vin + "/lock-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLockStatus(responseJson);
        }
    }

    public void actionHvacOn(double hvacTargetTemperature) throws RenaultForbiddenException,
            RenaultNotImplementedException, RenaultActionException, RenaultAPIGatewayException {
        final String path = "/commerce/v1/accounts/" + kamereonaccountId + "/kamereon/kca/car-adapter/v1/cars/"
                + config.vin + "/actions/hvac-start?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"HvacStart\",\"attributes\":{\"action\":\"start\",\"targetTemperature\":\""
                        + hvacTargetTemperature + "\"}}}");
    }

    public void actionChargeMode(ChargingMode mode) throws RenaultForbiddenException, RenaultNotImplementedException,
            RenaultActionException, RenaultAPIGatewayException {
        final String apiMode = ChargingMode.SCHEDULE_MODE.equals(mode) ? CHARGING_MODE_SCHEDULE : CHARGING_MODE_ALWAYS;
        final String path = "/commerce/v1/accounts/" + kamereonaccountId + "/kamereon/kca/car-adapter/v1/cars/"
                + config.vin + "/actions/charge-mode?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"ChargeMode\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    public void actionPause(boolean mode) throws RenaultForbiddenException, RenaultNotImplementedException,
            RenaultActionException, RenaultAPIGatewayException {
        final String apiMode = mode ? "pause" : "resume";
        final String path = "/commerce/v1/accounts/" + kamereonaccountId + "/kamereon/kcm/v1/vehicles/" + config.vin
                + "/charge/pause-resume?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"ChargePauseResume\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    private void postKamereonRequest(final String path, final String content) throws RenaultForbiddenException,
            RenaultNotImplementedException, RenaultActionException, RenaultAPIGatewayException {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(HttpMethod.POST)
                .header("Content-type", "application/vnd.api+json").header("apikey", this.config.kamereonApiKey)
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt)
                .content(new StringContentProvider(content, "utf-8"));
        try {
            ContentResponse response = request.send();
            logKamereonCall(request, response);
            checkResponse(response);
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
            throw new RenaultActionException(e.toString());
        }
    }

    private @Nullable JsonObject getKamereonResponse(String path) throws RenaultForbiddenException,
            RenaultNotImplementedException, RenaultUpdateException, RenaultAPIGatewayException {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(HttpMethod.GET)
                .header("Content-type", "application/vnd.api+json").header("apikey", this.config.kamereonApiKey)
                .header("x-kamereon-authorization", "Bearer " + kamereonToken).header("x-gigya-id_token", jwt);
        try {
            ContentResponse response = request.send();
            logKamereonCall(request, response);
            if (HttpStatus.OK_200 == response.getStatus()) {
                return JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            }
            checkResponse(response);
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
            throw new RenaultUpdateException(e.toString());
        }
        return null;
    }

    private void logKamereonCall(Request request, ContentResponse response) {
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Kamereon Request: {} Response:  [{}] {}\n{}", request.getURI().toString(),
                        response.getStatus(), response.getReason(), response.getContentAsString());
            }
        } else {
            logger.debug("Kamereon Request: {} Response: [{}] {}\n{}", request.getURI().toString(),
                    response.getStatus(), response.getReason(), response.getContentAsString());
        }
    }

    private void checkResponse(ContentResponse response)
            throws RenaultForbiddenException, RenaultNotImplementedException, RenaultAPIGatewayException {
        switch (response.getStatus()) {
            case HttpStatus.FORBIDDEN_403:
                throw new RenaultForbiddenException(
                        "Kamereon request forbidden! Ensure the car is paired in your MyRenault App.");
            case HttpStatus.NOT_FOUND_404:
                throw new RenaultNotImplementedException("Kamereon service not found");
            case HttpStatus.NOT_IMPLEMENTED_501:
                throw new RenaultNotImplementedException("Kamereon request not implemented");
            case HttpStatus.BAD_GATEWAY_502:
                throw new RenaultAPIGatewayException("Kamereon request failed");
            default:
                break;
        }
    }

    private String getCountry(RenaultConfiguration config) {
        String country = "XX";
        if (config.locale.length() == 5) {
            country = config.locale.substring(3);
        }
        return country;
    }
}
