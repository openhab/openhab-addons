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

package org.openhab.binding.tedee.internal.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Provides access to the Tedee Bridge local API.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeApi implements TedeeClient {
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String baseUrl;
    private final String apiToken;

    public TedeeApi(HttpClient client, String host, int port, String apiToken) {
        this.client = client;
        baseUrl = "http://" + host + ":" + port + "/v1.0";
        this.apiToken = apiToken;
    }

    public TedeeLock getLock(int id) throws TedeeApiException {
        ContentResponse response = executeGet("/lock/" + id);
        if (response.getStatus() != 200) {
            throw error(response);
        }
        String content = response.getContentAsString();

        TedeeLock lock = gson.fromJson(content, TedeeLock.class);
        if (lock == null) {
            throw new TedeeApiException("Invalid Tedee lock response", 500);
        }
        return lock;
    }

    public List<TedeeLock> getLocks() throws TedeeApiException {
        ContentResponse response = execute(newRequest("/lock", HttpMethod.GET));
        if (response.getStatus() != 200) {
            throw error(response);
        }

        String content = response.getContentAsString();

        JsonElement root;
        try {
            root = JsonParser.parseString(content);
        } catch (RuntimeException e) {
            throw new TedeeApiException("Invalid Tedee lock list response", 500, e);
        }

        List<TedeeLock> locks = new ArrayList<>();

        if (root.isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray()) {
                TedeeLock lock = gson.fromJson(element, TedeeLock.class);
                if (lock != null) {
                    locks.add(lock);
                }
            }
        } else if (root.isJsonObject()) {
            JsonElement lockArray = root.getAsJsonObject().get("locks");

            if (lockArray != null && lockArray.isJsonArray()) {
                for (JsonElement element : lockArray.getAsJsonArray()) {
                    TedeeLock lock = gson.fromJson(element, TedeeLock.class);
                    if (lock != null) {
                        locks.add(lock);
                    }
                }
            } else {
                TedeeLock lock = gson.fromJson(root, TedeeLock.class);
                if (lock != null) {
                    locks.add(lock);
                }
            }
        } else {
            throw new TedeeApiException("Invalid Tedee lock list response", 500);
        }

        return locks;
    }

    public int bridgeStatus() throws TedeeApiException {
        return execute(newRequest("/bridge", HttpMethod.GET)).getStatus();
    }

    public void lock(int id) throws TedeeApiException {
        post("/lock/" + id + "/lock");
    }

    public void unlock(int id) throws TedeeApiException {
        post("/lock/" + id + "/unlock");
    }

    public void unlockWithoutPull(int id) throws TedeeApiException {
        post("/lock/" + id + "/unlock?mode=3");
    }

    public void pull(int id) throws TedeeApiException {
        post("/lock/" + id + "/pull");
    }

    public void unlockOrPull(int id) throws TedeeApiException {
        post("/lock/" + id + "/unlock?mode=4");
    }

    public String getCallbacks() throws TedeeApiException {
        ContentResponse response = execute(newRequest("/callback", HttpMethod.GET));
        if (response.getStatus() != 200) {
            throw error(response);
        }
        return response.getContentAsString();
    }

    public String addCallback(String url, String webhookSecret) throws TedeeApiException {
        JsonObject callback = new JsonObject();
        callback.addProperty("url", url);
        callback.addProperty("method", "POST");

        JsonObject header = new JsonObject();
        header.addProperty("header_name", TedeeBindingConstants.WEBHOOK_SECRET_HEADER);
        header.addProperty("header_value", webhookSecret);
        JsonArray headers = new JsonArray();
        headers.add(header);
        callback.add("headers", headers);

        Request request = newRequest("/callback", HttpMethod.POST).header("Content-Type", "application/json");

        request.content(new StringContentProvider("application/json", callback.toString(), StandardCharsets.UTF_8));

        ContentResponse response = execute(request);

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw error(response);
        }

        return response.getContentAsString();
    }

    public void updateCallback(String callbackId, String url, String webhookSecret) throws TedeeApiException {
        JsonObject callback = new JsonObject();
        callback.addProperty("url", url);
        callback.addProperty("method", "POST");

        JsonObject header = new JsonObject();
        header.addProperty("header_name", TedeeBindingConstants.WEBHOOK_SECRET_HEADER);
        header.addProperty("header_value", webhookSecret);
        JsonArray headers = new JsonArray();
        headers.add(header);
        callback.add("headers", headers);

        Request request = newRequest("/callback/" + callbackId, HttpMethod.PUT).header("Content-Type",
                "application/json");
        request.content(new StringContentProvider("application/json", callback.toString(), StandardCharsets.UTF_8));

        ContentResponse response = execute(request);

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw error(response);
        }
    }

    public void deleteCallback(String callbackId) throws TedeeApiException {
        Request request = newRequest("/callback/" + callbackId, HttpMethod.DELETE);
        ContentResponse response = execute(request);

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw error(response);
        }
    }

    private void post(String path) throws TedeeApiException {
        Request request = newRequest(path, HttpMethod.POST);
        request.header("Content-Length", "0");
        ContentResponse response = execute(request);
        if (response.getStatus() != 204) {
            throw error(response);
        }
    }

    private Request newRequest(String path, HttpMethod method) {
        return client.newRequest(baseUrl + path).method(method).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private ContentResponse executeGet(String path) throws TedeeApiException {
        try {
            return execute(newRequest(path, HttpMethod.GET));
        } catch (TedeeApiException firstFailure) {
            // A Tedee Bridge can close an otherwise valid keep-alive connection without an HTTP response.
            // Retrying this idempotent GET once gives Jetty an opportunity to establish a fresh connection.
            if (firstFailure.getStatus() != 500) {
                throw firstFailure;
            }
            return execute(newRequest(path, HttpMethod.GET));
        }
    }

    private ContentResponse execute(Request request) throws TedeeApiException {
        request.header("api_token", encryptedToken());
        request.header("Accept", "application/json");
        try {
            return request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TedeeApiException("Interrupted", 408, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TedeeApiException("HTTP failed: " + e.getMessage(), 500, e);
        }
    }

    private String encryptedToken() throws TedeeApiException {
        long timestamp = System.currentTimeMillis();
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((apiToken + timestamp).getBytes(StandardCharsets.UTF_8)));
            return hash + timestamp;
        } catch (Exception e) {
            throw new TedeeApiException("SHA-256 unavailable", 500, e);
        }
    }

    private TedeeApiException error(ContentResponse response) {
        return new TedeeApiException("Tedee HTTP " + response.getStatus() + " " + response.getReason(),
                response.getStatus());
    }
}
