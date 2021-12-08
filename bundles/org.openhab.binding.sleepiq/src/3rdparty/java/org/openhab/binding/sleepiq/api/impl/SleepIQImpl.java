/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2022 Mark Hilbush
 * Reworked to replace JaxRS with Jetty client
 */
package org.openhab.binding.sleepiq.api.impl;

import java.net.CookieStore;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sleepiq.api.CommunicationException;
import org.openhab.binding.sleepiq.api.Configuration;
import org.openhab.binding.sleepiq.api.LoginException;
import org.openhab.binding.sleepiq.api.ResponseFormatException;
import org.openhab.binding.sleepiq.api.SleepIQ;
import org.openhab.binding.sleepiq.api.SleepIQException;
import org.openhab.binding.sleepiq.api.UnauthorizedException;
import org.openhab.binding.sleepiq.api.enums.Side;
import org.openhab.binding.sleepiq.api.enums.SleepDataInterval;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.BedsResponse;
import org.openhab.binding.sleepiq.api.model.Failure;
import org.openhab.binding.sleepiq.api.model.FamilyStatusResponse;
import org.openhab.binding.sleepiq.api.model.LoginInfo;
import org.openhab.binding.sleepiq.api.model.LoginRequest;
import org.openhab.binding.sleepiq.api.model.PauseModeResponse;
import org.openhab.binding.sleepiq.api.model.SleepDataResponse;
import org.openhab.binding.sleepiq.api.model.SleepNumberRequest;
import org.openhab.binding.sleepiq.api.model.Sleeper;
import org.openhab.binding.sleepiq.api.model.SleepersResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SleepIQImpl implements SleepIQ {
    private static final String PARAM_KEY = "_k";
    private static final String USER_AGENT = "SleepIQ/1593766370 CFNetwork/1185.2 Darwin/20.0.0";

    private static final Gson GSON = GsonGenerator.create(false);

    private final Logger logger = LoggerFactory.getLogger(SleepIQImpl.class);

    private final HttpClient httpClient;
    private final CookieStore cookieStore;

    protected final Configuration config;

    private final LoginRequest loginRequest = new LoginRequest();
    private volatile LoginInfo loginInfo;

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
    public LoginInfo login() throws LoginException, UnauthorizedException {
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
                            throw new UnauthorizedException(
                                    GSON.fromJson(response.getContentAsString(), Failure.class));
                        }
                        if (isNotOk(response)) {
                            throw new LoginException(GSON.fromJson(response.getContentAsString(), Failure.class));
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
        cloudRequest(Endpoints.setSleepNumber(bedId), null, body);
    }

    @Override
    public void setPauseMode(String bedId, boolean pauseMode) throws LoginException, SleepIQException {
        logger.debug("SleepIQ: setPauseMode: command={}", pauseMode);
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("mode", pauseMode ? "on" : "off");
        cloudRequest(Endpoints.setPauseMode(bedId), requestParameters, "");
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
        logger.debug("SleepIQ: cloudGetRequest: Invoke endpoint={}", endpoint);
        ContentResponse response = (body == null ? doGet(endpoint, parameters) : doPut(endpoint, parameters, body));
        if (isUnauthorized(response)) {
            logger.debug("SleepIQ: cloudGetRequest: UNAUTHORIZED, reset login");
            // Force new login and try again
            resetLogin();
            response = (body == null ? doGet(endpoint, parameters) : doPut(endpoint, parameters, body));
        }
        if (isNotOk(response)) {
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

    private ContentResponse doRequest(Request request, Map<String, String> parameters) throws CommunicationException {
        try {
            if (parameters != null) {
                for (String key : parameters.keySet()) {
                    request.param(key, parameters.get(key));
                }
            }
            addCookiesToRequest(request);
            logger.debug("SleepIQ: doPut: request url={}", request.getURI());
            ContentResponse response = request.send();
            logger.trace("SleepIQ: doPut: status={} response={}", response.getStatus(), response.getContentAsString());
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("SleepIQ: doPut: Exception message={}", e.getMessage(), e);
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
