/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ecowatt.internal.restapi;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ecowatt.internal.exception.EcowattApiLimitException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.i18n.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EcowattRestApi} is responsible for handling all communication with the Ecowatt REST API
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattRestApi {

    private static final String ECOWATT_API_TOKEN_URL = "https://digital.iservices.rte-france.com/token/oauth/";
    private static final String ECOWATT_API_GET_SIGNALS_URL = "https://digital.iservices.rte-france.com/open_api/ecowatt/v4/signals";

    private final Logger logger = LoggerFactory.getLogger(EcowattRestApi.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final Gson gson;
    private OAuthClientService authService;
    private String authServiceHandle;

    public EcowattRestApi(OAuthFactory oAuthFactory, HttpClient httpClient, String authServiceHandle, String idClient,
            String idSecret) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> OffsetDateTime
                        .parse(json.getAsJsonPrimitive().getAsString()).toZonedDateTime())
                .create();
        this.authService = oAuthFactory.createOAuthClientService(authServiceHandle, ECOWATT_API_TOKEN_URL, null,
                idClient, idSecret, null, true);
        this.authServiceHandle = authServiceHandle;
    }

    public EcowattApiResponse getSignals() throws CommunicationException, EcowattApiLimitException {
        logger.debug("API request signals");
        String token = authenticate().getAccessToken();

        final Request request = httpClient.newRequest(ECOWATT_API_GET_SIGNALS_URL).method(HttpMethod.GET)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token).timeout(10, TimeUnit.SECONDS);

        ContentResponse response;
        try {
            response = request.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new CommunicationException("@text/exception.api-request-failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommunicationException("@text/exception.api-request-failed", e);
        }

        int statusCode = response.getStatus();

        logger.trace("API response statusCode={} content={}", statusCode, response.getContentAsString());

        if (statusCode == HttpStatus.TOO_MANY_REQUESTS_429) {
            int retryAfter = -1;
            if (response.getHeaders().contains(HttpHeader.RETRY_AFTER)) {
                try {
                    retryAfter = Integer.parseInt(response.getHeaders().get(HttpHeader.RETRY_AFTER));
                } catch (NumberFormatException e) {
                }
            }
            throw new EcowattApiLimitException(retryAfter, "@text/exception.api-limit-reached");
        } else if (statusCode != HttpStatus.OK_200) {
            throw new CommunicationException("@text/exception.api-request-failed-params", statusCode,
                    response.getContentAsString());
        }

        try {
            EcowattApiResponse deserializedResp = gson.fromJson(response.getContentAsString(),
                    EcowattApiResponse.class);
            if (deserializedResp == null) {
                throw new CommunicationException("@text/exception.empty-api-response");
            }
            return deserializedResp;
        } catch (JsonSyntaxException e) {
            throw new CommunicationException("@text/exception.parsing-api-response-failed", e);
        }
    }

    private AccessTokenResponse authenticate() throws CommunicationException {
        try {
            AccessTokenResponse result = authService.getAccessTokenResponse();
            if (result == null || result.isExpired(Instant.now(), 120)) {
                logger.debug("Authentication required");
                result = authService.getAccessTokenByClientCredentials(null);
            }
            logger.debug("Token {} of type {} created on {} expiring after {} seconds", result.getAccessToken(),
                    result.getTokenType(), result.getCreatedOn(), result.getExpiresIn());
            return result;
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new CommunicationException("@text/exception.authentication-failed", e);
        }
    }

    public void dispose() {
        oAuthFactory.ungetOAuthService(authServiceHandle);
    }
}
