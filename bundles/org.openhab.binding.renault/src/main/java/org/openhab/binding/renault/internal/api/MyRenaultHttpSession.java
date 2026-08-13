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
 * @author Hilbrand Bouwkamp - Added SoC level channels
 */
@NonNullByDefault
public class MyRenaultHttpSession {

    private enum EndpointDefinitions {
        // state end points
        BATTERY_STATUS(CAR_ADAPTER_V2 + "battery-status"),
        COCKPIT(CAR_ADAPTER_V1 + "cockpit"),
        HVAC_STATUS(CAR_ADAPTER_V1 + "hvac-status"),
        LOCATION(CAR_ADAPTER_V1 + "location"),
        LOCK_STATUS(CAR_ADAPTER_V1 + "lock-status"),
        SOC_LEVELS("/kcm/v1/vehicles/%s/ev/soc-levels"),
        // action end points
        ACTIONS_CHARGE_MODE(CAR_ADAPTER_V1 + "actions/charge-mode"),
        ACTIONS_HVAC_START(CAR_ADAPTER_V1 + "actions/hvac-start"),
        CHARGE_PAUSE_RESUME("/kcm/v1/vehicles/%s/charge/pause-resume");

        private String endPoint;

        EndpointDefinitions(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getEndPoint(String vin) {
            return endPoint.formatted(vin);
        }
    }

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private static final String NOT_BE_THERE = "you should not be there but well done for the effort";
    private static final String ACCOUNTS_ROOT = "/commerce/v1/accounts/%s";
    private static final String KAMEREON = "/kamereon";
    private static final String CAR_ADAPTER_V1 = "/kca/car-adapter/v1/cars/%s/";
    private static final String CAR_ADAPTER_V2 = "/kca/car-adapter/v2/cars/%s/";

    private final Logger logger = LoggerFactory.getLogger(MyRenaultHttpSession.class);
    // Use a expiring cache to not login again if initSession is called within 3
    // seconds of the previous call.
    private final ExpiringCache<Boolean> initCache = new ExpiringCache<>(Duration.ofSeconds(3), () -> true);
    private final Object lock = new Object();

    private RenaultConfiguration config;
    private HttpClient httpClient;
    private Constants constants;
    private @Nullable String cookieValue;
    private @Nullable String personId;
    private @Nullable String gigyaDataCenter;
    private @Nullable String jwt;

    private @Nullable String accountsEndpointRoot;
    private @Nullable String kamereonEndpointRoot;

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
        getKamereonResponse("/commerce/v1/persons/" + personId + "?country=" + getCountry(config))
                .ifPresent(responseJson -> {
                    JsonArray accounts = responseJson.getAsJsonArray("accounts");
                    for (int i = 0; i < accounts.size(); i++) {
                        if (accounts.get(i).getAsJsonObject().get("accountType").getAsString()
                                .equals(config.accountType)) {
                            accountsEndpointRoot = ACCOUNTS_ROOT
                                    .formatted(accounts.get(i).getAsJsonObject().get("accountId").getAsString());
                            kamereonEndpointRoot = accountsEndpointRoot + KAMEREON;
                            break;
                        }
                    }
                });
        if (accountsEndpointRoot == null) {
            throw new RenaultException(String.format("@text/error.renault.session.kamereon_cant_get_account_id[\"%s\"]",
                    config.accountType));
        }
    }

    public void getVehicle(Car car) throws RenaultException {
        getKamereonResponse(accountsEndpointRoot + "/vehicles/" + config.vin + "/details?country=" + getCountry(config))
                .ifPresent(responseJson -> car.setDetails(responseJson));
    }

    public void getBatteryStatus(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.BATTERY_STATUS)
                .ifPresent(responseJson -> car.setBatteryStatus(responseJson));
    }

    public void getHvacStatus(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.HVAC_STATUS).ifPresent(responseJson -> car.setHVACStatus(responseJson));
    }

    public void getCockpit(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.COCKPIT).ifPresent(responseJson -> car.setCockpit(responseJson));
    }

    public void getLocation(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.LOCATION).ifPresent(responseJson -> car.setLocation(responseJson));
    }

    public void getLockStatus(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.LOCK_STATUS).ifPresent(responseJson -> car.setLockStatus(responseJson));
    }

    public void getSocLevels(Car car) throws RenaultException {
        getKamereonResponse(EndpointDefinitions.SOC_LEVELS).ifPresent(responseJson -> car.setSoc(responseJson));
    }

    public void actionHvacOn(double hvacTargetTemperature) throws RenaultException {
        postKamereonRequest(EndpointDefinitions.ACTIONS_HVAC_START,
                "{\"data\":{\"type\":\"HvacStart\",\"attributes\":{\"action\":\"start\",\"targetTemperature\":"
                        + hvacTargetTemperature + "}}}");
    }

    public void actionChargeMode(ChargingMode mode) throws RenaultException {
        final String apiMode = mode.name().toLowerCase(Locale.ROOT);
        postKamereonRequest(EndpointDefinitions.ACTIONS_CHARGE_MODE,
                "{\"data\":{\"type\":\"ChargeMode\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    public void actionPause(boolean mode) throws RenaultException {
        final String apiMode = mode ? "pause" : "resume";
        postKamereonRequest(EndpointDefinitions.CHARGE_PAUSE_RESUME,
                "{\"data\":{\"type\":\"ChargePauseResume\",\"attributes\":{\"action\":\"" + apiMode + "\"}}}");
    }

    public void actionSetSocLevels(Car car, int socMin, int socTarget) throws RenaultException {
        postKamereonRequest(EndpointDefinitions.SOC_LEVELS,
                "{\"socMin\": %d, \"socTarget\": %d}".formatted(socMin, socTarget));
    }

    private String kamereonUrl(EndpointDefinitions endpointDefinition) {
        return String.format("%s%s?country=%s", kamereonEndpointRoot, endpointDefinition.getEndPoint(config.vin),
                getCountry(config));
    }

    private void postKamereonRequest(final EndpointDefinitions endpointDefinition, final String content)
            throws RenaultException {
        requestKamereonResponse(HttpMethod.POST, kamereonUrl(endpointDefinition),
                new StringContentProvider(content, "utf-8"));
    }

    private Optional<JsonObject> getKamereonResponse(EndpointDefinitions endpointDefinition) throws RenaultException {
        return getKamereonResponse(kamereonUrl(endpointDefinition));
    }

    private Optional<JsonObject> getKamereonResponse(String path) throws RenaultException {
        return requestKamereonResponse(HttpMethod.GET, path, null);
    }

    private Optional<JsonObject> requestKamereonResponse(HttpMethod httpMethod, String path,
            @Nullable StringContentProvider content) throws RenaultException {
        Request request = httpClient.newRequest(this.constants.getKamereonRootUrl() + path).method(httpMethod)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Content-type", "application/vnd.api+json")
                .header("apikey", this.config.kamereonApiKey).header("x-gigya-id_token", jwt).content(content);
        try {
            ContentResponse response = request.send();
            logKamereonCall(request, response);
            return checkResponse(response);
        } catch (InterruptedException e) {
            logger.warn("Kamereon Request: {} threw exception: {} ", request.getURI().toString(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (JsonParseException | TimeoutException | ExecutionException e) {
            throw new RenaultUpdateException(e.toString());
        }
        return Optional.empty();
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

    private Optional<JsonObject> checkResponse(ContentResponse response)
            throws RenaultForbiddenException, RenaultNotImplementedException, RenaultAPIGatewayException {
        return switch (response.getStatus()) {
            case HttpStatus.OK_200 -> {
                final JsonObject json = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
                if (Optional.ofNullable(json.get("message")).map(JsonElement::getAsString).filter(NOT_BE_THERE::equals)
                        .isPresent()) {
                    logger.debug("Kamereon response indicates unsupported operation: {}", NOT_BE_THERE);
                    throw new RenaultNotImplementedException(
                            "@text/error.renault.session.kamereon_request_not_implemented");
                }
                yield Optional.ofNullable(json);
            }
            case HttpStatus.FORBIDDEN_403 -> {
                try {
                    final @Nullable JsonObject json = Optional.ofNullable(response.getContentAsString())
                            .map(JsonParser::parseString).map(JsonElement::getAsJsonObject).orElse(null);
                    if ("err.func.privacy.on".equals(getErrorCode(json))) {
                        throw new RenaultForbiddenException("@text/error.renault.session.kamereon_privacy_on");
                    }
                } catch (JsonParseException e) {
                    logger.debug("Could not parse 403 message: {}", response.getContentAsString());
                }
                throw new RenaultForbiddenException("@text/error.renault.session.kamereon_request_forbidden");
            }
            case HttpStatus.NOT_FOUND_404 ->
                throw new RenaultNotImplementedException("@text/error.renault.session.kamereon_service_not_found");
            case HttpStatus.TOO_MANY_REQUESTS_429 ->
                throw new RenaultAPIGatewayException("@text/error.renault.session.kamereon_quota_limit_exceeded");
            case HttpStatus.NOT_IMPLEMENTED_501 -> throw new RenaultNotImplementedException(
                    "@text/error.renault.session.kamereon_request_not_implemented");
            case HttpStatus.BAD_GATEWAY_502 ->
                throw new RenaultAPIGatewayException("@text/error.renault.session.kamereon_request_failed");
            default -> Optional.empty();
        };
    }

    private String getCountry(RenaultConfiguration config) {
        String country = "XX";
        if (config.locale.length() == 5) {
            country = config.locale.substring(3);
        }
        return country;
    }

    private static String getErrorCode(@Nullable JsonObject responseJson) {
        // @formatter:off
        final @Nullable String errorCode = Optional.ofNullable(responseJson)
            .map(m -> m.get("messages"))
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
