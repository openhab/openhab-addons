/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.withings.internal.service.AccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractAPIHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractAPIHandler.class);

    private final AccessTokenService accessTokenService;
    private final HttpClient httpClient;

    public AbstractAPIHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        this.accessTokenService = accessTokenService;
        this.httpClient = httpClient;
    }

    protected <@NonNull T extends BaseResponse> Optional<T> executeAuthPOSTRequest(String url, String action,
            Map<String, String> parameters, Class<T> responseType) {
        return executePOSTRequest(url, action, parameters, responseType, false);
    }

    protected <@NonNull T extends BaseResponse> Optional<T> executePOSTRequest(String url, String action,
            Map<String, String> parameters, Class<T> responseType) {
        return executePOSTRequest(url, action, parameters, responseType, true);
    }

    protected <@NonNull T extends BaseResponse> Optional<T> executePOSTRequest(String url, String action,
            Map<String, String> parameters, Class<T> responseType, boolean useAccessToken) {

        Optional<String> accessToken;
        if (useAccessToken) {
            accessToken = accessTokenService.getAccessToken();
            if (!accessToken.isPresent()) {
                logger.warn("No access token available for the access to the Withings data API!");
                return Optional.empty();
            }
        } else {
            accessToken = Optional.empty();
        }

        Fields fields = new Fields(true);
        fields.put("action", action);
        for (Map.Entry<String, String> parameterMapEntry : parameters.entrySet()) {
            fields.put(parameterMapEntry.getKey(), parameterMapEntry.getValue());
        }

        Request request = httpClient.POST(url).timeout(20, TimeUnit.SECONDS).idleTimeout(60, TimeUnit.SECONDS)
                .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .content(new FormContentProvider(fields));
        if (useAccessToken) {
            request = request.header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken.get());
        }

        try {
            ContentResponse response = request.send();
            @Nullable
            T responseObject = new Gson().fromJson(response.getContentAsString(), responseType);

            if (responseObject != null && responseObject.isSuccessful()) {
                return Optional.of(responseObject);
            } else {
                logger.warn("Error on executing API data request. Response information: {}", responseObject);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error on accessing API data! Message: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
