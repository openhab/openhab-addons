/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
import org.openhab.binding.renault.internal.api.exceptions.RenaultException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultForbiddenException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultNotImplementedException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultUpdateException;
import org.openhab.core.cache.ExpiringCache;
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

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private static final String NOT_BE_THERE = "you should not be there but well done for the effort";

    private final Logger logger = LoggerFactory.getLogger(MyRenaultHttpSession.class);
    // Use a expiring cache to not login again if initSession is called within 3
    // seconds of the previous call.
    private final ExpiringCache<Boolean> initCache = new ExpiringCache<>(Duration.ofSeconds(3), () -> true);
    private final Object lock = new Object();

    private RenaultConfiguration config;
    private HttpClient httpClient;
    private Constants constants;
    private @Nullable String kamereonaccountId;
    private @Nullable String cookieValue;
    private @Nullable String personId;
    private @Nullable String gigyaDataCenter;
    private @Nullable String jwt;

    public MyRenaultHttpSession(RenaultConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.constants = new Constants(config.locale);
    }

    public void initSesssion() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        synchronized (lock) {
            if (initCache.isExpired()) {
                login();
                getAccountInfo();
                getJWT();
                getAccountID();
                initCache.refreshValue();
            }
        }
    }

    private void login() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        final Fields fields = new Fields();
        fields.add("ApiKey", getGigyaApiKey());
        fields.add("loginID", config.myRenaultUsername);
        fields.add("password", config.myRenaultPassword);
        final String url = this.constants.getGigyaRootUrl() + "/accounts.login";
        final ContentResponse response = httpClient.FORM(url, fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
            try {
                final JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                final JsonObject sessionInfoJson = responseJson.getAsJsonObject("sessionInfo");
                if (sessionInfoJson == null) {
                    throw new RenaultException("@text/error.renault.session.login.no_session_info");
                }
                JsonElement element = sessionInfoJson.get("cookieValue");
                if (element == null) {
                    throw new RenaultException("@text/error.renault.session.login.no_cookie");
                }
                cookieValue = element.getAsString();
                logger.debug("Cookie: {}", cookieValue);
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException("@text/error.renault.session.login.no_cookie");
            }
            if (cookieValue == null) {
                logger.warn("Login Error: cookie value not found! Response: {}", response.getContentAsString());
                throw new RenaultException("@text/error.renault.session.login.no_cookie");
            }
        } else {
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException(
                    String.format("@text/error.renault.session.login.error[\"%s\"]", response.getReason()));
        }
    }

    private void getAccountInfo() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        Fields fields = new Fields();
        fields.add("ApiKey", getGigyaApiKey());
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
                throw new RenaultException("@text/error.renault.session.gigya.no_data");
            }
        } else {
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException(
                    String.format("@text/error.renault.session.gigya.error[\"%s\"]", response.getReason()));
        }
    }

    /**
     * Return the gigyaApiKey from configuration if it is used to override the
     * default hard-coded constant.
     *
     * @return
     */
    private String getGigyaApiKey() {
        if (!this.config.gigyaApiKey.isBlank()) {
            return this.config.gigyaApiKey;
        }
        return this.constants.getGigyaApiKey();
    }

    private void getJWT() throws RenaultException, InterruptedException, ExecutionException, TimeoutException {
        final Fields fields = new Fields();
        fields.add("ApiKey", getGigyaApiKey());
        fields.add("login_token", cookieValue);
        fields.add("fields", "data.personId,data.gigyaDataCenter");
        fields.add("personId", personId);
        fields.add("gigyaDataCenter", gigyaDataCenter);
        final String url = this.constants.getGigyaRootUrl() + "/accounts.getJWT";
        final ContentResponse response = this.httpClient.FORM(url, fields);
        if (HttpStatus.OK_200 == response.getStatus()) {
            if (logger.isTraceEnabled()) {
                logger.trace("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(),
                        response.getReason(), response.getContentAsString());
            }
            try {
                final JsonObject responseJson = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                logger.trace("GigyaApi response: {}", responseJson);
                final JsonElement element = responseJson.get("id_token");
                if (element != null) {
                    jwt = element.getAsString();
                    logger.debug("GigyaApi jwt: {} ", jwt);
                }
            } catch (JsonParseException | ClassCastException | IllegalStateException e) {
                throw new RenaultException("@text/error.renault.session.jwt.no_jwt");
            }
        } else {
            logger.warn("GigyaApi Request: {} Response: [{}] {}\n{}", url, response.getStatus(), response.getReason(),
                    response.getContentAsString());
            throw new RenaultException(
                    String.format("@text/error.renault.session.jwt.error[\"%s\"]", response.getReason()));
        }
    }

    private void getAccountID() throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(
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
            throw new RenaultException(String.format("@text/error.renault.session.kamereon_cant_get_account_id[\"%s\"]",
                    config.accountType));
        }
    }

    public void getVehicle(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse("/commerce/v1/accounts/" + kamereonaccountId + "/vehicles/"
                + config.vin + "/details?country=" + getCountry(config));
        if (responseJson != null) {
            car.setDetails(responseJson);
        }
    }

    public void getBatteryStatus(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(kcav2() + "battery-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setBatteryStatus(responseJson);
        }
    }

    public void getHvacStatus(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(kcav1() + "hvac-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setHVACStatus(responseJson);
        }
    }

    public void getCockpit(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(kcav1() + "cockpit?country=" + getCountry(config));
        if (responseJson != null) {
            car.setCockpit(responseJson);
        }
    }

    public void getLocation(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(kcav1() + "location?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLocation(responseJson);
        }
    }

    public void getLockStatus(Car car) throws RenaultException {
        final JsonObject responseJson = getKamereonResponse(kcav1() + "lock-status?country=" + getCountry(config));
        if (responseJson != null) {
            car.setLockStatus(responseJson);
        }
    }

    public void actionHvacOn(double hvacTargetTemperature) throws RenaultException {
        final String path = kcav1() + "actions/hvac-start?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"HvacStart\",\"attributes\":{\"action\":\"start\",\"targetTemperature\":"
                        + hvacTargetTemperature + "}}}");
    }

    public void actionChargeMode(ChargingMode mode) throws RenaultException {
        final String apiMode = mode.name().toLowerCase(Locale.ROOT);
        final String path = kcav1() + "actions/charge-mode?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"ChargeMode\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    public void actionPause(boolean mode) throws RenaultException {
        final String apiMode = mode ? "pause" : "resume";
        final String path = "/commerce/v1/accounts/" + kamereonaccountId + "/kamereon/kcm/v1/vehicles/" + config.vin
                + "/charge/pause-resume?country=" + getCountry(config);
        postKamereonRequest(path,
                "{\"data\":{\"type\":\"ChargePauseResume\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    private void postKamereonRequest(final String path, final String content) throws RenaultException {
        requestKamereonResponse(HttpMethod.POST, path, new StringContentProvider(content, "utf-8"));
    }

    private @Nullable JsonObject getKamereonResponse(String path) throws RenaultException {
        return requestKamereonResponse(HttpMethod.GET, path, null);
    }

    private @Nullable JsonObject requestKamereonResponse(HttpMethod httpMethod, String path,
            @Nullable StringContentProvider content) throws RenaultException {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(httpMethod)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Content-type", "application/vnd.api+json")
                .header("apikey", this.config.kamereonApiKey).header("x-gigya-id_token", jwt).content(content);
        try {
            ContentResponse response = request.send();
            logKamereonCall(request, response);
            if (httpMethod == HttpMethod.GET) {
                if (HttpStatus.OK_200 == response.getStatus()) {
                    final JsonObject json = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                    checkNotSupported(json);
                    return json;
                }
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

    private String kcav1() {
        return String.format("/commerce/v1/accounts/%s/kamereon/kca/car-adapter/v1/cars/%s/", kamereonaccountId,
                config.vin);
    }

    private String kcav2() {
        return String.format("/commerce/v1/accounts/%s/kamereon/kca/car-adapter/v2/cars/%s/", kamereonaccountId,
                config.vin);
    }

    private void checkNotSupported(JsonObject json) throws RenaultNotImplementedException {
        if (Optional.ofNullable(json.get("message")).map(JsonElement::getAsString).filter(NOT_BE_THERE::equals)
                .isPresent()) {
            logger.debug("Kamereon response indicates unsupported operation: {}", NOT_BE_THERE);
            throw new RenaultNotImplementedException("@text/error.renault.session.kamereon_request_not_implemented");
        }
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
                try {
                    final JsonObject json = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                    if ("err.func.privacy.on".equals(getErrorCode(json))) {
                        throw new RenaultForbiddenException("@text/error.renault.session.kamereon_privacy_on");
                    }
                } catch (JsonParseException e) {
                    logger.debug("Could not parse 403 message: {}", response.getContentAsString());
                }
                throw new RenaultForbiddenException("@text/error.renault.session.kamereon_request_forbidden");
            case HttpStatus.NOT_FOUND_404:
                throw new RenaultNotImplementedException("@text/error.renault.session.kamereon_service_not_found");
            case HttpStatus.NOT_IMPLEMENTED_501:
                throw new RenaultNotImplementedException(
                        "@text/error.renault.session.kamereon_request_not_implemented");
            case HttpStatus.BAD_GATEWAY_502:
                throw new RenaultAPIGatewayException("@text/error.renault.session.kamereon_request_failed");
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

    private static String getErrorCode(JsonObject responseJson) {
        // @formatter:off
        final @Nullable String errorCode = Optional.ofNullable(responseJson.get("messages"))
            .map(m -> m.getAsJsonArray())
            .map(m -> m.asList())
            .map(m -> m.get(0))
            .map(m -> m.getAsJsonObject())
            .map(m -> m.get("code"))
            .map(m -> m.getAsString())
            .orElse("");
        // @formatter:on
        return errorCode == null ? "" : errorCode;
    }
}
