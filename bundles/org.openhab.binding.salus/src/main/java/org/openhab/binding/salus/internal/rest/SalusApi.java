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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private @Nullable String get(String url, RestClient.Header header, int retryAttempt) throws SalusApiException {
        refreshAccessToken();
        try {
            return restClient.get(url, authHeader());
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401) {
                if (retryAttempt <= MAX_RETRIES) {
                    forceRefreshAccessToken();
                    return get(url, header, retryAttempt + 1);
                }
                logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            }
            throw ex;
        }
    }

    private @Nullable String post(String url, RestClient.Content content, RestClient.Header header, int retryAttempt)
            throws SalusApiException {
        refreshAccessToken();
        try {
            return restClient.post(url, content, header);
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401) {
                if (retryAttempt <= MAX_RETRIES) {
                    forceRefreshAccessToken();
                    return post(url, content, header, retryAttempt + 1);
                }
                logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            }
            throw ex;
        }
    }

    private static String removeTrailingSlash(String str) {
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    private void login(String username, char[] password) throws SalusApiException {
        login(username, password, 1);
    }

    private void login(String username, char[] password, int retryAttempt) throws SalusApiException {
        logger.debug("Login with username '{}', retryAttempt={}", username, retryAttempt);
        authToken = null;
        authTokenExpireTime = null;
        var finalUrl = url("/users/sign_in.json");
        var inputBody = mapper.loginParam(username, password);
        try {
            var response = restClient.post(finalUrl, new RestClient.Content(inputBody, "application/json"),
                    new RestClient.Header("Accept", "application/json"));
            if (response == null) {
                throw new HttpSalusApiException(401, "No response token from server");
            }
            var token = authToken = mapper.authToken(response);
            authTokenExpireTime = LocalDateTime.now(clock).plusSeconds(token.expiresIn())
                    // this is to account that there is a delay between server setting `expires_in`
                    // and client (OpenHAB) receiving it
                    .minusSeconds(TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS);
            logger.debug("Correctly logged in for user {}, role={}, expires at {} ({} secs)", username, token.role(),
                    authTokenExpireTime, token.expiresIn());
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401 || ex.getCode() == 403) {
                if (retryAttempt < MAX_RETRIES) {
                    login(username, password, retryAttempt + 1);
                }
                throw ex;
            }
            throw ex;
        }
    }

    private void forceRefreshAccessToken() throws SalusApiException {
        logger.debug("Force refresh access token");
        authToken = null;
        authTokenExpireTime = null;
        refreshAccessToken();
    }

    private void refreshAccessToken() throws SalusApiException {
        if (this.authToken == null || isExpiredToken()) {
            try {
                login(username, password);
            } catch (SalusApiException ex) {
                logger.warn("Accesstoken could not be acquired, for user '{}', response={}", username, ex.getMessage());
                this.authToken = null;
                this.authTokenExpireTime = null;
                throw ex;
            }
        }
    }

    private boolean isExpiredToken() {
        var expireTime = authTokenExpireTime;
        return expireTime == null || LocalDateTime.now(clock).isAfter(expireTime);
    }

    private String url(String url) {
        return baseUrl + url;
    }

    public SortedSet<Device> findDevices() throws SalusApiException {
        refreshAccessToken();
        var response = get(url("/apiv1/devices.json"), authHeader(), 1);
        return new TreeSet<>(mapper.parseDevices(requireNonNull(response)));
    }

    private RestClient.Header authHeader() {
        return new RestClient.Header("Authorization", "auth_token " + requireNonNull(authToken).accessToken());
    }

    public SortedSet<DeviceProperty<?>> findDeviceProperties(String dsn) throws SalusApiException {
        refreshAccessToken();
        var response = get(url("/apiv1/dsns/" + dsn + "/properties.json"), authHeader(), 1);
        if (response == null) {
            throw new SalusApiException("No device properties for device %s".formatted(dsn));
        }
        return new TreeSet<>(mapper.parseDeviceProperties(response));
    }

    public Object setValueForProperty(String dsn, String propertyName, Object value) throws SalusApiException {
        refreshAccessToken();
        var finalUrl = url("/apiv1/dsns/" + dsn + "/properties/" + propertyName + "/datapoints.json");
        var json = mapper.datapointParam(value);
        var response = post(finalUrl, new RestClient.Content(json), authHeader(), 1);
        var datapointValue = mapper.datapointValue(response);
        return datapointValue.orElseThrow(() -> new HttpSalusApiException(404, "No datapoint in return"));
    }
}
