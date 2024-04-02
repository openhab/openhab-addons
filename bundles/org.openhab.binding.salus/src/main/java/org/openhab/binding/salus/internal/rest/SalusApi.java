/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private static final int MAX_RETRIES = 3;
    private static final long TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS = 3;
    private final Logger logger;
    private final String username;
    private final char[] password;
    private final String baseUrl;
    private final RestClient restClient;
    private final GsonMapper mapper;
    @Nullable
    private AuthToken authToken;
    @Nullable
    private LocalDateTime authTokenExpireTime;
    private final Clock clock;

    public SalusApi(String username, char[] password, String baseUrl, RestClient restClient, GsonMapper mapper,
            Clock clock) {
        this.username = username;
        this.password = password;
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.restClient = restClient;
        this.mapper = mapper;
        this.clock = clock;
        // thanks to this, logger will always inform for which rest client it's doing the job
        // it's helpful when more than one SalusApi exists
        logger = LoggerFactory.getLogger(SalusApi.class.getName() + "[" + username.replace(".", "_") + "]");
    }

    public SalusApi(String username, char[] password, String baseUrl, RestClient restClient, GsonMapper mapper) {
        this(username, password, baseUrl, restClient, mapper, Clock.systemDefaultZone());
    }

    private RestClient.Response<@Nullable String> get(String url, RestClient.Header header, int retryAttempt)
            throws ExecutionException, InterruptedException, TimeoutException {
        refreshAccessToken();
        var response = restClient.get(url, authHeader());
        if (response.statusCode() == 401) {
            logger.debug("Refreshing access token");
            if (retryAttempt <= MAX_RETRIES) {
                forceRefreshAccessToken();
                return get(url, header, retryAttempt + 1);
            }
            logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            return response;
        }
        return response;
    }

    private RestClient.Response<@Nullable String> post(String url, RestClient.Content content, RestClient.Header header,
            int retryAttempt) throws ExecutionException, InterruptedException, TimeoutException {
        refreshAccessToken();
        var response = restClient.post(url, content, header);
        if (response.statusCode() == 401) {
            if (retryAttempt <= MAX_RETRIES) {
                forceRefreshAccessToken();
                return post(url, content, header, retryAttempt + 1);
            }
            logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            return response;
        }
        return response;
    }

    private static String removeTrailingSlash(String str) {
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    private RestClient.Response<@Nullable String> login(String username, char[] password)
            throws ExecutionException, InterruptedException, TimeoutException {
        return login(username, password, 1);
    }

    private RestClient.Response<@Nullable String> login(String username, char[] password, int retryAttempt)
            throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Login with username '{}', retryAttempt={}", username, retryAttempt);
        authToken = null;
        authTokenExpireTime = null;
        var finalUrl = url("/users/sign_in.json");
        var inputBody = mapper.loginParam(username, password);
        var response = restClient.post(finalUrl, new RestClient.Content(inputBody, "application/json"),
                new RestClient.Header("Accept", "application/json"));
        if (response.statusCode() == 401) {
            if (retryAttempt < MAX_RETRIES) {
                return login(username, password, retryAttempt + 1);
            }
            return response;
        }
        if (response.statusCode() == 403) {
            if (retryAttempt < MAX_RETRIES) {
                return login(username, password, retryAttempt + 1);
            }
            return response;
        }
        if (response.statusCode() != 200) {
            return new RestClient.Response<@Nullable String>(response.statusCode(), response.body());
        }
        var token = authToken = mapper.authToken(requireNonNull(response.body()));
        authTokenExpireTime = LocalDateTime.now(clock).plusSeconds(token.expiresIn())
                // this is to account that there is a delay between server setting `expires_in`
                // and client (OpenHAB) receiving it
                .minusSeconds(TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS);
        logger.debug("Correctly logged in for user {}, role={}, expires at {} ({} secs)", username, token.role(),
                authTokenExpireTime, token.expiresIn());
        return response;
    }

    private void forceRefreshAccessToken() throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Force refresh access token");
        authToken = null;
        authTokenExpireTime = null;
        refreshAccessToken();
    }

    private RestClient.@Nullable Response<@Nullable String> refreshAccessToken()
            throws ExecutionException, InterruptedException, TimeoutException {
        if (this.authToken == null || isExpiredToken()) {
            var response = login(username, password);
            if (response.statusCode() != 200) {
                logger.warn("Accesstoken could not be acquired, for user '{}', response={}", username, response);
                this.authToken = null;
                this.authTokenExpireTime = null;
            }
            return response;
        }
        return null;
    }

    private boolean isExpiredToken() {
        var expireTime = authTokenExpireTime;
        return expireTime == null || LocalDateTime.now(clock).isAfter(expireTime);
    }

    private String url(String url) {
        return baseUrl + url;
    }

    public ApiResponse<SortedSet<Device>> findDevices()
            throws ExecutionException, InterruptedException, TimeoutException {
        var loginResponse = refreshAccessToken();
        if (loginResponse != null && loginResponse.statusCode() != 200) {
            return error(new Error(loginResponse.statusCode(), loginResponse.body()));
        }
        var response = get(url("/apiv1/devices.json"), authHeader(), 1);
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            return error(mapper.parseError(response));
        }

        var devices = new TreeSet<>(mapper.parseDevices(requireNonNull(response.body())));
        return ApiResponse.ok(devices);
    }

    private RestClient.Header authHeader() {
        return new RestClient.Header("Authorization", "auth_token " + requireNonNull(authToken).accessToken());
    }

    public ApiResponse<SortedSet<DeviceProperty<?>>> findDeviceProperties(String dsn)
            throws ExecutionException, InterruptedException, TimeoutException {
        var loginResponse = refreshAccessToken();
        if (loginResponse != null && loginResponse.statusCode() != 200) {
            return error(new Error(loginResponse.statusCode(), loginResponse.body()));
        }
        var response = get(url("/apiv1/dsns/" + dsn + "/properties.json"), authHeader(), 1);
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            return error(mapper.parseError(response));
        }

        var deviceProperties = new TreeSet<>(mapper.parseDeviceProperties(requireNonNull(response.body())));
        return ApiResponse.ok(deviceProperties);
    }

    public ApiResponse<Object> setValueForProperty(String dsn, String propertyName, Object value)
            throws ExecutionException, InterruptedException, TimeoutException {
        var loginResponse = refreshAccessToken();
        if (loginResponse != null && loginResponse.statusCode() != 200) {
            return error(new Error(loginResponse.statusCode(), loginResponse.body()));
        }
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
