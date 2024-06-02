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
package org.openhab.binding.salus.internal.cloud.rest;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.rest.AbstractSalusApi;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.HttpSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * The SalusApi class is responsible for interacting with a REST API to perform various operations related to the Salus
 * system. It handles authentication, token management, and provides methods to retrieve and manipulate device
 * information and properties.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class HttpSalusApi extends AbstractSalusApi<AuthToken> {
    private static final int MAX_RETRIES = 3;
    private static final long TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS = 3;
    @Nullable
    private AuthToken authToken;

    public HttpSalusApi(String username, byte[] password, String baseUrl, RestClient restClient, GsonMapper mapper,
            Clock clock) {
        super(username, password, baseUrl, restClient, mapper, clock);
    }

    public HttpSalusApi(String username, byte[] password, String baseUrl, RestClient restClient, GsonMapper mapper) {
        super(username, password, baseUrl, restClient, mapper, Clock.systemDefaultZone());
    }

    @Override
    protected @Nullable String get(String url, RestClient.Header... headers)
            throws SalusApiException, AuthSalusApiException {
        return this.get(url, 1, headers);
    }

    @Override
    protected @Nullable String post(String url, RestClient.Content content, RestClient.Header... headers)
            throws SalusApiException, AuthSalusApiException {
        return this.post(url, content, 1, headers);
    }

    private @Nullable String get(String url, int retryAttempt, RestClient.Header... headers)
            throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        try {
            return restClient.get(url, headers);
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401) {
                if (retryAttempt <= MAX_RETRIES) {
                    forceRefreshAccessToken();
                    return get(url, retryAttempt + 1, headers);
                }
                logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            }
            throw ex;
        }
    }

    private @Nullable String post(String url, RestClient.Content content, int retryAttempt,
            RestClient.Header... headers) throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        try {
            return restClient.post(url, content, headers);
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401) {
                if (retryAttempt <= MAX_RETRIES) {
                    forceRefreshAccessToken();
                    return post(url, content, retryAttempt + 1, headers);
                }
                logger.debug("Could not refresh access token after {} retries", MAX_RETRIES);
            }
            throw ex;
        }
    }

    @Override
    protected void login() throws SalusApiException {
        login(1);
    }

    private void login(int retryAttempt) throws SalusApiException {
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
            var local = LocalDateTime.now(clock).plusSeconds(token.expiresIn())
                    // this is to account that there is a delay between server setting `expires_in`
                    // and client (OpenHAB) receiving it
                    .minusSeconds(TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS);
            authTokenExpireTime = ZonedDateTime.of(local, UTC);
            logger.debug("Correctly logged in for user {}, role={}, expires at {} ({} secs)", username, token.role(),
                    authTokenExpireTime, token.expiresIn());
        } catch (HttpSalusApiException ex) {
            if (ex.getCode() == 401 || ex.getCode() == 403) {
                if (retryAttempt < MAX_RETRIES) {
                    login(retryAttempt + 1);
                }
                throw ex;
            }
            throw ex;
        }
    }

    private void forceRefreshAccessToken() throws AuthSalusApiException {
        logger.debug("Force refresh access token");
        cleanAuth();
        refreshAccessToken();
    }

    @Override
    public SortedSet<Device> findDevices() throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        var response = get(url("/apiv1/devices.json"), authHeader());
        return new TreeSet<>(mapper.parseDevices(requireNonNull(response)));
    }

    private RestClient.Header authHeader() {
        return new RestClient.Header("Authorization", "auth_token " + requireNonNull(authToken).accessToken());
    }

    @Override
    public SortedSet<DeviceProperty<?>> findDeviceProperties(String dsn)
            throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        var response = get(url("/apiv1/dsns/" + dsn + "/properties.json"), authHeader());
        if (response == null) {
            throw new SalusApiException("No device properties for device %s".formatted(dsn));
        }
        return new TreeSet<>(mapper.parseDeviceProperties(response));
    }

    @Override
    public Object setValueForProperty(String dsn, String propertyName, Object value)
            throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        var finalUrl = url("/apiv1/dsns/" + dsn + "/properties/" + propertyName + "/datapoints.json");
        var json = mapper.datapointParam(value);
        var response = post(finalUrl, new RestClient.Content(json), 1, authHeader());
        var datapointValue = mapper.datapointValue(response);
        return datapointValue.orElseThrow(() -> new HttpSalusApiException(404, "No datapoint in return"));
    }
}
