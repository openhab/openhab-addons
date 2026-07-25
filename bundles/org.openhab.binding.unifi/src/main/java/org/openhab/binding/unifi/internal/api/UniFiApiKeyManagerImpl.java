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
package org.openhab.binding.unifi.internal.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implementation of {@link UniFiApiKeyManager} that talks to the UniFi OS user management
 * endpoints using an already-authenticated {@link UniFiSession}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiApiKeyManagerImpl implements UniFiApiKeyManager {

    @FunctionalInterface
    private interface ApiCall<T> {
        T execute() throws Exception;
    }

    private static final long REQUEST_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(UniFiApiKeyManagerImpl.class);
    private final HttpClient httpClient;
    private final UniFiSession session;
    private final Gson gson = new Gson();

    private volatile @Nullable String cachedUserId;

    public UniFiApiKeyManagerImpl(HttpClient httpClient, UniFiSession session) {
        this.httpClient = httpClient;
        this.session = session;
    }

    @Override
    public String getUserId() throws UniFiException {
        String uid = cachedUserId;
        if (uid != null) {
            return uid;
        }
        return wrapExceptions("getUserId", () -> {
            ContentResponse response = doGet(session.getBaseUrl() + "/api/users/self", "GET /api/users/self");
            JsonObject json = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            String id = json.has("id") ? json.get("id").getAsString() : null;
            if (id == null || id.isBlank()) {
                throw new UniFiException("User ID not found in /api/users/self response");
            }
            cachedUserId = id;
            logger.debug("Fetched user ID: {}", id);
            return id;
        });
    }

    @Override
    public List<UniFiApiKey> listApiKeys(String userId) throws UniFiException {
        return wrapExceptions("listApiKeys", () -> {
            String url = session.getBaseUrl() + "/proxy/users/api/v2/user/" + userId + "/keys";
            ContentResponse response = doGet(url, "listApiKeys");
            JsonObject root = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            JsonElement data = root.get("data");
            if (data == null || !data.isJsonArray()) {
                return List.of();
            }
            UniFiApiKey[] keys = gson.fromJson(data, UniFiApiKey[].class);
            return keys != null ? List.of(keys) : List.of();
        });
    }

    @Override
    public UniFiApiKey createApiKey(String userId, String name) throws UniFiException {
        return wrapExceptions("createApiKey", () -> {
            String url = session.getBaseUrl() + "/proxy/users/api/v2/user/" + userId + "/keys";
            String body = gson.toJson(Map.of("name", name));
            ContentResponse response = doPost(url, body, "createApiKey");
            JsonObject root = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            JsonElement data = root.get("data");
            if (data == null || !data.isJsonObject()) {
                throw new UniFiException("Unexpected response format from createApiKey");
            }
            UniFiApiKey key = gson.fromJson(data, UniFiApiKey.class);
            if (key == null) {
                throw new UniFiException("Failed to parse API key from response");
            }
            logger.debug("Created API key '{}' (id={})", name, key.id);
            return key;
        });
    }

    @Override
    public void deleteApiKey(String keyId) throws UniFiException {
        wrapExceptions("deleteApiKey", () -> {
            String url = session.getBaseUrl() + "/proxy/users/api/v2/keys/" + keyId;
            Request request = httpClient.newRequest(url).method(HttpMethod.DELETE).timeout(REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            session.addAuthHeaders(request);
            ContentResponse response = request.send();
            ensureSuccess(response, "DELETE /proxy/users/api/v2/keys/" + keyId);
            updateCsrfFromResponse(response);
            logger.debug("Deleted API key {}", keyId);
            return null;
        });
    }

    private <T> T wrapExceptions(String action, ApiCall<T> call) throws UniFiException {
        try {
            return call.execute();
        } catch (UniFiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UniFiException("Interrupted during " + action, e);
        } catch (Exception e) {
            throw new UniFiException(action + " failed: " + e.getMessage(), e);
        }
    }

    private ContentResponse doGet(String url, String action) throws Exception {
        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        session.addAuthHeaders(request);
        ContentResponse response = request.send();
        ensureSuccess(response, action);
        updateCsrfFromResponse(response);
        return response;
    }

    private ContentResponse doPost(String url, String body, String action) throws Exception {
        Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(REQUEST_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        session.addAuthHeaders(request);
        request.header("Content-Type", "application/json");
        request.content(new StringContentProvider(body));
        ContentResponse response = request.send();
        ensureSuccess(response, action);
        updateCsrfFromResponse(response);
        return response;
    }

    private void ensureSuccess(ContentResponse response, String action) throws UniFiException {
        int status = response.getStatus();
        if (status == HttpStatus.UNAUTHORIZED_401) {
            throw new UniFiException(action + " returned 401 Unauthorized");
        }
        if (!HttpStatus.isSuccess(status)) {
            throw new UniFiException(action + " returned HTTP " + status);
        }
    }

    private void updateCsrfFromResponse(ContentResponse response) {
        String csrf = response.getHeaders().get("X-Updated-CSRF-Token");
        if (csrf != null && !csrf.isBlank()) {
            session.updateCsrfToken(csrf);
        }
    }
}
