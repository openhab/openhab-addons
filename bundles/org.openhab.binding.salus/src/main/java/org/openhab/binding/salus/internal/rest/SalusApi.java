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
package org.openhab.binding.salus.internal.rest;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.salus.internal.rest.ApiResponse.error;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SalusApi class is responsible for interacting with a REST API to perform various operations related to the Salus
 * system. It handles authentication, token management, and provides methods to retrieve and manipulate device
 * information and properties.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class SalusApi {
    private static final int MAX_TIMES = 3;
    private final Logger logger;
    private final String username;
    private final char[] password;
    private final String baseUrl;
    private final RestClient restClient;
    private final GsonMapper mapper;
    private AuthToken authToken;
    private LocalDateTime authTokenExpireTime;
    private final Clock clock;

    public SalusApi(String username, char[] password, String baseUrl, RestClient restClient, GsonMapper mapper,
            Clock clock) {
        this.username = requireNonNull(username, "username");
        this.password = requireNonNull(password, "password");
        this.baseUrl = removeTrailingSlash(requireNonNull(baseUrl, "baseUrl"));
        this.restClient = requireNonNull(restClient, "restClient can not be null!");
        this.mapper = requireNonNull(mapper, "mapper can not be null!");
        this.clock = requireNonNull(clock, "clock can not be null!");
        // thanks to this, logger will always inform for which rest client it's doing the job
        // it's helpful when more than one SalusApi exists
        logger = LoggerFactory.getLogger(SalusApi.class.getName() + "[" + username.replace(".", "_") + "]");
    }

    public SalusApi(String username, char[] password, String baseUrl, RestClient restClient, GsonMapper mapper) {
        this(username, password, baseUrl, restClient, mapper, Clock.systemDefaultZone());
    }

    private RestClient.Response<String> get(String url, RestClient.Header header, int times) {
        refreshAccessToken();
        var response = restClient.get(url, authHeader());
        if (response.statusCode() == 401) {
            logger.info("Refreshing access token");
            login(username, password);
            if (times > MAX_TIMES) {
                logger.warn("Could not refresh access token after {} times", MAX_TIMES);
                return response;
            }
            return get(url, header, times + 1);
        }
        return response;
    }

    private RestClient.Response<String> post(String url, RestClient.Content content, RestClient.Header header,
            int times) {
        refreshAccessToken();
        var response = restClient.post(url, content, header);
        if (response.statusCode() == 401) {
            logger.info("Refreshing access token");
            login(username, password);
            if (times > MAX_TIMES) {
                logger.warn("Could not refresh access token after {} times", MAX_TIMES);
                return response;
            }
            return post(url, content, header, times + 1);
        }
        return response;
    }

    private static String removeTrailingSlash(String str) {
        if (str != null && str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    private void login(String username, char[] password) {
        login(username, password, 1);
    }

    private void login(String username, char[] password, int times) {
        logger.info("Login with username '{}', times={}", username, times);
        authToken = null;
        authTokenExpireTime = null;
        var finalUrl = url("/users/sign_in.json");
        var method = "POST";
        var inputBody = mapper.loginParam(username, password);
        var response = restClient.post(finalUrl, new RestClient.Content(inputBody, "application/json"),
                new RestClient.Header("Accept", "application/json"));
        if (response.statusCode() == 401) {
            if (times < MAX_TIMES) {
                login(username, password, times + 1);
                return;
            }
            throw new HttpUnauthorizedException(method, finalUrl);
        }
        if (response.statusCode() == 403) {
            if (times < MAX_TIMES) {
                login(username, password, times + 1);
                return;
            }
            throw new HttpForbiddenException(method, finalUrl);
        }
        if (response.statusCode() / 100 == 4) {
            throw new HttpClientException(response.statusCode(), method, finalUrl);
        }
        if (response.statusCode() / 100 == 5) {
            throw new HttpServerException(response.statusCode(), method, finalUrl);
        }
        if (response.statusCode() != 200) {
            throw new HttpUnknownException(response.statusCode(), method, finalUrl);
        }
        authToken = mapper.authToken(response.body());
        authTokenExpireTime = LocalDateTime.now(clock).plusSeconds(authToken.expiresIn());
        logger.info("Correctly logged in for user {}, role={}, expires at {} ({} secs)", username, authToken.role(),
                authTokenExpireTime, authToken.expiresIn());
    }

    private void refreshAccessToken() {
        if (this.authToken == null) {
            login(username, password);
        } else if (expiredToken()) {
            login(username, password);
        } else if (shouldRefreshTokenBeforeExpire()) {
            refreshBeforeExpire();
        } else {
            logger.debug("Refreshing token is not required");
        }
    }

    private boolean expiredToken() {
        return LocalDateTime.now(clock).isAfter(authTokenExpireTime);
    }

    private boolean shouldRefreshTokenBeforeExpire() {
        return false;
    }

    private void refreshBeforeExpire() {
        logger.warn("Refreshing token before expire is not supported!");
    }

    private String url(String url) {
        return baseUrl + url;
    }

    public ApiResponse<SortedSet<Device>> findDevices() {
        logger.debug("findDevices()");
        refreshAccessToken();
        var response = get(url("/apiv1/devices.json"), authHeader(), 1);
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            logger.debug("findDevices()->ERROR {}", response.statusCode());
            return error(mapper.parseError(response));
        }

        var devices = new TreeSet<>(mapper.parseDevices(response.body()));
        logger.debug("findDevices()->OK");
        return ApiResponse.ok(devices);
    }

    private RestClient.Header authHeader() {
        return new RestClient.Header("Authorization", "auth_token " + authToken.accessToken());
    }

    public ApiResponse<SortedSet<DeviceProperty<?>>> findDeviceProperties(String dsn) {
        logger.debug("findDeviceProperties({})", dsn);
        refreshAccessToken();
        var response = get(url("/apiv1/dsns/" + dsn + "/properties.json"), authHeader(), 1);
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            logger.debug("findDeviceProperties()->ERROR {}", response.statusCode());
            return error(mapper.parseError(response));
        }

        var deviceProperties = new TreeSet<>(mapper.parseDeviceProperties(response.body()));
        logger.debug("findDeviceProperties({})->OK", dsn);
        return ApiResponse.ok(deviceProperties);
    }

    public ApiResponse<Object> setValueForProperty(String dsn, String propertyName, Object value) {
        refreshAccessToken();
        var finalUrl = url("/apiv1/dsns/" + dsn + "/properties/" + propertyName + "/datapoints.json");
        var json = mapper.datapointParam(value);
        var response = post(finalUrl, new RestClient.Content(json), authHeader(), 1);
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            return error(mapper.parseError(response));
        }
        var datapointValue = response.map(mapper::datapointValue).body();
        return datapointValue.map(ApiResponse::ok).orElseGet(() -> error(new Error(404, "No datapoint in return")));
    }
}
