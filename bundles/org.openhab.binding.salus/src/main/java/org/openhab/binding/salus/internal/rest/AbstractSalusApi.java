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

import java.time.Clock;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.SalusApi;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSalusApi<AuthT> implements SalusApi {
    protected static final long TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS = 3;
    protected final Logger logger;
    protected final String username;
    protected final byte[] password;
    protected final String baseUrl;
    protected final RestClient restClient;
    protected final GsonMapper mapper;
    @Nullable
    protected ZonedDateTime authTokenExpireTime;
    protected final Clock clock;
    @Nullable
    protected AuthT authentication;

    protected AbstractSalusApi(String username, byte[] password, String baseUrl, RestClient restClient,
            GsonMapper mapper, Clock clock) {
        this.username = username;
        this.password = password;
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.restClient = restClient;
        this.mapper = mapper;
        this.clock = clock;
        // thanks to this, logger will always inform for which rest client it's doing the job
        // it's helpful when more than one SalusApi exists
        logger = LoggerFactory.getLogger(this.getClass().getName() + "[" + username.replace(".", "_") + "]");
    }

    public AbstractSalusApi(String username, byte[] password, String baseUrl, RestClient restClient,
            GsonMapper mapper) {
        this(username, password, baseUrl, restClient, mapper, Clock.systemDefaultZone());
    }

    protected @Nullable String get(String url, RestClient.Header... headers)
            throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        return restClient.get(url, headers);
    }

    protected @Nullable String post(String url, RestClient.Content content, RestClient.Header... headers)
            throws SalusApiException, AuthSalusApiException {
        refreshAccessToken();
        return restClient.post(url, content, headers);
    }

    private static String removeTrailingSlash(String str) {
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    protected final synchronized void refreshAccessToken() throws AuthSalusApiException {
        if (this.authentication == null || isExpiredToken()) {
            cleanAuth();
            try {
                login();
            } catch (Exception ex) {
                cleanAuth();
                throw new AuthSalusApiException("Could not log in, for user " + username, ex);
            }
        }
    }

    protected void cleanAuth() {
        authentication = null;
        authTokenExpireTime = null;
    }

    protected abstract void login() throws SalusApiException, AuthSalusApiException;

    private boolean isExpiredToken() {
        var expireTime = authTokenExpireTime;
        return expireTime == null || ZonedDateTime.now(clock).isAfter(expireTime);
    }

    protected final String url(String url) {
        return baseUrl + url;
    }
}
