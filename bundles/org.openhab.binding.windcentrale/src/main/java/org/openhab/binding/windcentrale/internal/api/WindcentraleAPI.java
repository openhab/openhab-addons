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
package org.openhab.binding.windcentrale.internal.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.openhab.binding.windcentrale.internal.dto.WindcentraleGson.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.windcentrale.internal.dto.Project;
import org.openhab.binding.windcentrale.internal.dto.Windmill;
import org.openhab.binding.windcentrale.internal.dto.WindmillStatus;
import org.openhab.binding.windcentrale.internal.exception.FailedGettingDataException;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindcentraleAPI} implements the Windcentrale REST API which allows for querying project participations and
 * the current windmill status.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindcentraleAPI {

    public static final String URL_PREFIX = "https://mijn.windcentrale.nl/api/v0";
    private static final String LIVE_DATA_URL = URL_PREFIX + "/livedata";
    private static final String PROJECTS_URL = URL_PREFIX + "/sustainable/projects";

    private static final String APPLICATION_JSON = "application/json";
    private static final String BEARER = "Bearer ";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);

    private final Logger logger = LoggerFactory.getLogger(WindcentraleAPI.class);

    private final HttpClient httpClient;
    private final TokenProvider tokenProvider;

    private final Set<RequestListener> requestListeners = ConcurrentHashMap.newKeySet();

    public WindcentraleAPI(HttpClientFactory httpClientFactory, TokenProvider tokenProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.tokenProvider = tokenProvider;
    }

    public void dispose() {
        requestListeners.clear();
    }

    public void addRequestListener(RequestListener listener) {
        requestListeners.add(listener);
    }

    public void removeRequestListener(RequestListener listener) {
        requestListeners.remove(listener);
    }

    private String getAuthorizationHeader() throws InvalidAccessTokenException {
        return BEARER + tokenProvider.getIdToken();
    }

    private String getJson(String url) throws FailedGettingDataException, InvalidAccessTokenException {
        try {
            logger.debug("Getting JSON from: {}", url);
            ContentResponse contentResponse = httpClient.newRequest(url) //
                    .method(GET) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .header(AUTHORIZATION, getAuthorizationHeader()) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send();

            if (contentResponse.getStatus() >= 400) {
                FailedGettingDataException exception = new FailedGettingDataException(
                        String.format("Windcentrale API error: %s (HTTP %s)", contentResponse.getReason(),
                                contentResponse.getStatus()));
                requestListeners.forEach(listener -> listener.onError(exception));
                throw exception;
            }
            String response = contentResponse.getContentAsString();
            logger.trace("Response: {}", response);
            requestListeners.forEach(RequestListener::onSuccess);
            return response;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            FailedGettingDataException exception = new FailedGettingDataException(
                    "Windcentrale API request failed: " + e.getMessage(), e);
            requestListeners.forEach(listener -> listener.onError(exception));
            throw exception;
        } catch (InvalidAccessTokenException e) {
            requestListeners.forEach(listener -> listener.onError(e));
            throw e;
        }
    }

    public Map<Windmill, WindmillStatus> getLiveData() throws FailedGettingDataException, InvalidAccessTokenException {
        return getLiveData(Set.of());
    }

    public @Nullable WindmillStatus getLiveData(Windmill windmill)
            throws FailedGettingDataException, InvalidAccessTokenException {
        return getLiveData(Set.of(windmill)).get(windmill);
    }

    public Map<Windmill, WindmillStatus> getLiveData(Set<Windmill> windmills)
            throws FailedGettingDataException, InvalidAccessTokenException {
        logger.debug("Getting live data: {}", windmills);

        String queryParams = "";
        if (!windmills.isEmpty()) {
            queryParams = "?projects="
                    + windmills.stream().map(Windmill::getProjectCode).collect(Collectors.joining(","));
        }

        String json = getJson(LIVE_DATA_URL + queryParams);
        return Objects.requireNonNullElse(GSON.fromJson(json, LIVE_DATA_RESPONSE_TYPE), Map.of());
    }

    public List<Project> getProjects() throws FailedGettingDataException, InvalidAccessTokenException {
        logger.debug("Getting projects");
        String json = getJson(PROJECTS_URL);
        return Objects.requireNonNullElse(GSON.fromJson(json, PROJECTS_RESPONSE_TYPE), List.of());
    }
}
