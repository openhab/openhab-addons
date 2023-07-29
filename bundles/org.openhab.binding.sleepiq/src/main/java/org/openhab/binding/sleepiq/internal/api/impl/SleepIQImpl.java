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
package org.openhab.binding.sleepiq.internal.api.impl;

import java.net.CookieStore;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sleepiq.internal.api.CommunicationException;
import org.openhab.binding.sleepiq.internal.api.Configuration;
import org.openhab.binding.sleepiq.internal.api.LoginException;
import org.openhab.binding.sleepiq.internal.api.ResponseFormatException;
import org.openhab.binding.sleepiq.internal.api.SleepIQ;
import org.openhab.binding.sleepiq.internal.api.SleepIQException;
import org.openhab.binding.sleepiq.internal.api.UnauthorizedException;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.dto.BedsResponse;
import org.openhab.binding.sleepiq.internal.api.dto.Failure;
import org.openhab.binding.sleepiq.internal.api.dto.FamilyStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationFeaturesResponse;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationOutletRequest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationPositionRequest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationPresetRequest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.LoginInfo;
import org.openhab.binding.sleepiq.internal.api.dto.LoginRequest;
import org.openhab.binding.sleepiq.internal.api.dto.PauseModeResponse;
import org.openhab.binding.sleepiq.internal.api.dto.SleepDataResponse;
import org.openhab.binding.sleepiq.internal.api.dto.SleepNumberRequest;
import org.openhab.binding.sleepiq.internal.api.dto.Sleeper;
import org.openhab.binding.sleepiq.internal.api.dto.SleepersResponse;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuator;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutlet;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutletOperation;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.api.enums.SleepDataInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SleepIQImpl} class handles all interactions with the sleepiq service.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Mark Hilbush - Added foundation functionality
 */
@NonNullByDefault
public class SleepIQImpl implements SleepIQ {
    private static final String PARAM_KEY = "_k";
    private static final String USER_AGENT = "SleepIQ/1593766370 CFNetwork/1185.2 Darwin/20.0.0";

    private static final Gson GSON = GsonGenerator.create(false);

    private final Logger logger = LoggerFactory.getLogger(SleepIQImpl.class);

    private final HttpClient httpClient;
    private final CookieStore cookieStore;

    protected final Configuration config;

    private final LoginRequest loginRequest = new LoginRequest();
    private volatile @Nullable LoginInfo loginInfo;

    public SleepIQImpl(Configuration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        cookieStore = httpClient.getCookieStore();
        loginRequest.setLogin(config.getUsername());
        loginRequest.setPassword(config.getPassword());
    }

    @Override
    public void shutdown() {
        cookieStore.removeAll();
    }

    @Override
    public @Nullable LoginInfo login() throws LoginException, UnauthorizedException {
        logger.trace("SleepIQ: login: loginInfo={}", loginInfo);
        if (loginInfo == null) {
            synchronized (this) {
                if (loginInfo == null) {
                    Request request = httpClient.newRequest(config.getBaseUri()).path(Endpoints.login())
                            .agent(USER_AGENT).header(HttpHeader.CONTENT_TYPE, "application/json")
                            .timeout(10, TimeUnit.SECONDS).method(HttpMethod.PUT)
                            .content(new StringContentProvider(GSON.toJson(loginRequest)), "application/json");
                    logger.trace("SleepIQ: login: request url={}", request.getURI());

                    try {
                        ContentResponse response = request.send();
                        logger.debug("SleepIQ: login: status={}, content={}", response.getStatus(),
                                response.getContentAsString());
                        if (isUnauthorized(response)) {
                            Failure failure = GSON.fromJson(response.getContentAsString(), Failure.class);
                            String message = failure != null ? failure.getError().getMessage() : "Login unauthorized";
                            throw new UnauthorizedException(message);
                        }
                        if (isNotOk(response)) {
                            Failure failure = GSON.fromJson(response.getContentAsString(), Failure.class);
                            String message = failure != null ? failure.getError().getMessage() : "Login failed";
                            throw new LoginException(message);
                        }
                        try {
                            loginInfo = GSON.fromJson(response.getContentAsString(), LoginInfo.class);
                        } catch (JsonSyntaxException e) {
                            throw new LoginException("Failed to parse 'login' response");
                        }
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        logger.info("SleepIQ: login: Login failed message={}", e.getMessage(), e);
                        throw new LoginException("Problem communicating with SleepIQ cloud service");
                    }
                }
            }
        }
        return loginInfo;
    }

    @Override
    public List<Bed> getBeds() throws LoginException, CommunicationException, ResponseFormatException {
        try {
            String contentResponse = cloudRequest(Endpoints.bed());
            BedsResponse response = GSON.fromJson(contentResponse, BedsResponse.class);
            if (response != null) {
                return response.getBeds();
            } else {
                throw new ResponseFormatException("Failed to get a valid 'beds' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'beds' response");
        }
    }

    @Override
    public FamilyStatusResponse getFamilyStatus()
            throws LoginException, ResponseFormatException, CommunicationException {
        try {
            String contentResponse = cloudRequest(Endpoints.familyStatus());
            FamilyStatusResponse response = GSON.fromJson(contentResponse, FamilyStatusResponse.class);
            if (response != null) {
                return response;
            } else {
                throw new ResponseFormatException("Failed to get a valid 'familyStatus' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'familyStatus' response");
        }
    }

    @Override
    public List<Sleeper> getSleepers() throws LoginException, ResponseFormatException, CommunicationException {
        try {
            String contentResponse = cloudRequest(Endpoints.sleeper());
            SleepersResponse response = GSON.fromJson(contentResponse, SleepersResponse.class);
            if (response != null) {
                return response.getSleepers();
            } else {
                throw new ResponseFormatException("Failed to get a valid 'sleepers' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'sleepers' response");
        }
    }

    @Override
    public PauseModeResponse getPauseMode(String bedId)
            throws LoginException, ResponseFormatException, CommunicationException {
        try {
            String contentResponse = cloudRequest(Endpoints.pauseMode(bedId));
            PauseModeResponse response = GSON.fromJson(contentResponse, PauseModeResponse.class);
            if (response != null) {
                return response;
            } else {
                throw new ResponseFormatException("Failed to get a valid 'pauseMode' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'pauseMode' response");
        }
    }

    @Override
    public SleepDataResponse getSleepData(String sleeperId, SleepDataInterval interval)
            throws LoginException, ResponseFormatException, CommunicationException {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("interval", interval.value());
            parameters.put("sleeper", sleeperId);
            parameters.put("includeSlices", "false");
            parameters.put("date", formatSleepDataDate(ZonedDateTime.now()));
            String contentResponse = cloudRequest(Endpoints.sleepData(), parameters);
            SleepDataResponse response = GSON.fromJson(contentResponse, SleepDataResponse.class);
            if (response != null) {
                return response;
            } else {
                throw new ResponseFormatException("Failed to get a valid 'sleepData' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'sleepData' response");
        }
    }

    private String formatSleepDataDate(ZonedDateTime zonedDateTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(zonedDateTime);
    }

    @Override
    public void setSleepNumber(String bedId, Side side, int sleepNumber)
            throws LoginException, ResponseFormatException, CommunicationException {
        String body = GSON.toJson(new SleepNumberRequest().withBedId(bedId).withSleepNumber(sleepNumber).withSide(side),
                SleepNumberRequest.class);
        logger.debug("SleepIQ: setSleepNumber: Request body={}", body);
        cloudRequest(Endpoints.sleepNumber(bedId), null, body);
    }

    @Override
    public void setPauseMode(String bedId, boolean pauseMode)
            throws LoginException, ResponseFormatException, CommunicationException {
        logger.debug("SleepIQ: setPauseMode: command={}", pauseMode);
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("mode", pauseMode ? "on" : "off");
        cloudRequest(Endpoints.pauseMode(bedId), requestParameters, "");
    }

    @Override
    public FoundationFeaturesResponse getFoundationFeatures(String bedId)
            throws LoginException, ResponseFormatException, CommunicationException {
        try {
            String contentResponse = cloudRequest(Endpoints.foundationFeatures(bedId));
            FoundationFeaturesResponse response = GSON.fromJson(contentResponse, FoundationFeaturesResponse.class);
            if (response != null) {
                logger.debug("SleepIQ: {}", response);
                return response;
            } else {
                throw new ResponseFormatException("Failed to get a valid 'foundationFeatures' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'foundationFeatures' response");
        }
    }

    @Override
    public FoundationStatusResponse getFoundationStatus(String bedId) throws LoginException, SleepIQException {
        try {
            String contentResponse = cloudRequest(Endpoints.foundationStatus(bedId));
            FoundationStatusResponse response = GSON.fromJson(contentResponse, FoundationStatusResponse.class);
            if (response != null) {
                logger.debug("SleepIQ: {}", response);
                return response;
            } else {
                throw new ResponseFormatException("Failed to get a valid 'foundationStatus' response from cloud");
            }
        } catch (JsonSyntaxException e) {
            throw new ResponseFormatException("Failed to parse 'foundationStatus' response");
        }
    }

    @Override
    public void setFoundationPreset(String bedId, Side side, FoundationPreset preset, FoundationActuatorSpeed speed)
            throws LoginException, SleepIQException {
        String body = GSON.toJson(new FoundationPresetRequest().withSide(side).withFoundationPreset(preset)
                .withFoundationActuatorSpeed(speed), FoundationPresetRequest.class);
        logger.debug("SleepIQ: setFoundationPreset: Request body={}", body);
        cloudRequest(Endpoints.foundationPreset(bedId), null, body);
    }

    @Override
    public void setFoundationPosition(String bedId, Side side, FoundationActuator actuator, int position,
            FoundationActuatorSpeed speed) throws LoginException, SleepIQException {
        String body = GSON.toJson(new FoundationPositionRequest().withSide(side).withPosition(position)
                .withFoundationActuator(actuator).withFoundationActuatorSpeed(speed), FoundationPositionRequest.class);
        logger.debug("SleepIQ: setFoundationPosition: Request body={}", body);
        cloudRequest(Endpoints.foundationPosition(bedId), null, body);
    }

    @Override
    public void setFoundationOutlet(String bedId, FoundationOutlet outlet, FoundationOutletOperation operation)
            throws LoginException, SleepIQException {
        String body = GSON.toJson(
                new FoundationOutletRequest().withFoundationOutlet(outlet).withFoundationOutletOperation(operation),
                FoundationOutletRequest.class);
        logger.debug("SleepIQ: setFoundationOutlet: Request body={}", body);
        cloudRequest(Endpoints.foundationOutlet(bedId), null, body);
    }

    private String cloudRequest(String endpoint)
            throws LoginException, ResponseFormatException, CommunicationException {
        return cloudRequest(endpoint, null, null);
    }

    private String cloudRequest(String endpoint, Map<String, String> parameters)
            throws LoginException, ResponseFormatException, CommunicationException {
        return cloudRequest(endpoint, parameters, null);
    }

    private String cloudRequest(String endpoint, @Nullable Map<String, String> parameters, @Nullable String body)
            throws LoginException, ResponseFormatException, CommunicationException {
        logger.debug("SleepIQ: cloudRequest: Invoke endpoint={}", endpoint);
        ContentResponse response = (body == null ? doGet(endpoint, parameters) : doPut(endpoint, parameters, body));
        if (isUnauthorized(response)) {
            logger.debug("SleepIQ: cloudGetRequest: UNAUTHORIZED, reset login");
            // Force new login and try again
            resetLogin();
            response = (body == null ? doGet(endpoint, parameters) : doPut(endpoint, parameters, body));
        }
        if (isNotOk(response)) {
            logger.debug("SleepIQ.cloudRequest: ResponseFormatException on call to endpoint {}", endpoint);
            throw new ResponseFormatException(String.format("Cloud API returned error: status=%d, message=%s",
                    response.getStatus(), HttpStatus.getCode(response.getStatus()).getMessage()));
        }
        return response.getContentAsString();
    }

    private ContentResponse doGet(String endpoint, @Nullable Map<String, String> parameters)
            throws CommunicationException, LoginException {
        LoginInfo login = login();
        Request request = httpClient.newRequest(config.getBaseUri()).path(endpoint).param(PARAM_KEY, login.getKey())
                .agent(USER_AGENT).header(HttpHeader.CONTENT_TYPE, "application/json").timeout(10, TimeUnit.SECONDS)
                .method(HttpMethod.GET);
        return doRequest(request, parameters);
    }

    private ContentResponse doPut(String endpoint, @Nullable Map<String, String> parameters, String body)
            throws CommunicationException, LoginException {
        LoginInfo login = login();
        Request request = httpClient.newRequest(config.getBaseUri()).path(endpoint).param(PARAM_KEY, login.getKey())
                .agent(USER_AGENT).header(HttpHeader.CONTENT_TYPE, "application/json").timeout(10, TimeUnit.SECONDS)
                .method(HttpMethod.PUT).content(new StringContentProvider(body), "application/json");
        return doRequest(request, parameters);
    }

    private synchronized ContentResponse doRequest(Request request, @Nullable Map<String, String> parameters)
            throws CommunicationException {
        try {
            if (parameters != null) {
                for (String key : parameters.keySet()) {
                    request.param(key, parameters.get(key));
                }
            }
            addCookiesToRequest(request);
            logger.debug("SleepIQ: doRequest: request url={}", request.getURI());
            ContentResponse response = request.send();
            logger.trace("SleepIQ: doRequest: status={} response={}", response.getStatus(),
                    response.getContentAsString());
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("SleepIQ: doRequest: Exception message={}", e.getMessage(), e);
            throw new CommunicationException("Communication error while accessing API: " + e.getMessage());
        }
    }

    private void addCookiesToRequest(Request request) {
        cookieStore.get(config.getBaseUri()).forEach(cookie -> {
            request.cookie(cookie);
        });
    }

    private boolean isUnauthorized(ContentResponse response) {
        return response.getStatus() == HttpStatus.Code.UNAUTHORIZED.getCode();
    }

    private boolean isNotOk(ContentResponse response) {
        return response.getStatus() != HttpStatus.Code.OK.getCode();
    }

    private synchronized void resetLogin() {
        logger.debug("SleepIQ: resetLogin: Set loginInfo=null to force login on next transaction");
        loginInfo = null;
    }
}
