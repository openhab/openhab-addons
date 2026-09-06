/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.loqed.internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Minimal client for the LOQED Integrations API.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedApiClient {
    private static final String API_BASE_URL = "https://integrations.production.loqed.com/api/locks";
    private static final int TIMEOUT_SECONDS = 15;
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final String apiToken;

    public LoqedApiClient(HttpClient httpClient, String apiToken) {
        this.httpClient = httpClient;
        this.apiToken = apiToken;
    }

    public List<LoqedLockData> getLocks() throws LoqedApiException {
        ContentResponse response = send(API_BASE_URL + "/");
        return parseLocks(response.getContentAsString());
    }

    public void setBoltState(String lockId, BoltState boltState) throws LoqedApiException {
        send(API_BASE_URL + "/" + lockId + "/bolt_state/" + boltState.apiValue());
    }

    private ContentResponse send(String url) throws LoqedApiException {
        try {
            ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET)
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + apiToken).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .send();
            if (!HttpStatus.isSuccess(response.getStatus())) {
                throw responseException(response);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoqedCommunicationException("Communication with the LOQED API was interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new LoqedCommunicationException("Could not communicate with the LOQED API", e);
        }
    }

    private static LoqedApiException responseException(ContentResponse response) {
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
            return new LoqedAuthenticationException(statusCode);
        }
        return new LoqedResponseException(statusCode, response.getContentAsString());
    }

    static List<LoqedLockData> parseLocks(String json) throws LoqedResponseException {
        try {
            @Nullable
            JsonElement response = JsonParser.parseString(json);
            if (!response.isJsonObject()) {
                throw new LoqedResponseException("The LOQED API returned an invalid response");
            }
            JsonElement data = response.getAsJsonObject().get("data");
            if (data == null || !data.isJsonArray()) {
                throw new LoqedResponseException("The LOQED API response does not contain a lock list");
            }

            List<LoqedLockData> locks = new ArrayList<>();
            for (JsonElement element : data.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    throw new LoqedResponseException("The LOQED API returned invalid lock data");
                }
                @Nullable
                LoqedLockData lock = GSON.fromJson(element, LoqedLockData.class);
                if (lock == null) {
                    throw new LoqedResponseException("The LOQED API returned invalid lock data");
                }
                lock.boltState = Objects.requireNonNullElse(lock.boltState, BoltState.UNKNOWN);
                locks.add(lock);
            }
            return List.copyOf(locks);
        } catch (JsonParseException | IllegalStateException e) {
            throw new LoqedResponseException("The LOQED API returned invalid JSON", e);
        }
    }
}
