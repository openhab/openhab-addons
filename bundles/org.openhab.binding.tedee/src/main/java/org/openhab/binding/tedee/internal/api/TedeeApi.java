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
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String base, token;
    private final Logger logger = LoggerFactory.getLogger(TedeeApi.class);

    public TedeeApi(HttpClient c, String ip, int port, String t) {
        client = c;
        base = "http://" + ip + ":" + port + "/v1.0";
        token = t;
    }

    public TedeeLock getLock(int id) throws TedeeApiException {
        ContentResponse r = execute(client.newRequest(base + "/lock/" + id).method(HttpMethod.GET));
        if (r.getStatus() != 200) {
            throw error(r);
        }
        String content = r.getContentAsString();

        TedeeLock lock = gson.fromJson(content, TedeeLock.class);
        if (lock == null) {
            throw new TedeeApiException("Invalid Tedee lock response", 500);
        }
        return lock;
    }

    public List<TedeeLock> getLocks() throws TedeeApiException {
        ContentResponse r = execute(client.newRequest(base + "/lock").method(HttpMethod.GET));
        if (r.getStatus() != 200) {
            throw error(r);
        }

        String content = r.getContentAsString();

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
        return execute(client.newRequest(base + "/bridge").method(HttpMethod.GET)).getStatus();
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
        ContentResponse r = execute(client.newRequest(base + "/callback").method(HttpMethod.GET));
        if (r.getStatus() != 200) {
            throw error(r);
        }
        return r.getContentAsString();
    }

    public String addCallback(String url) throws TedeeApiException {
        JsonObject callback = new JsonObject();
        callback.addProperty("url", url);
        callback.addProperty("method", "POST");
        callback.add("headers", new JsonArray());

        Request request = client.newRequest(base + "/callback").method(HttpMethod.POST).header("Content-Type",
                "application/json");

        request.content(new StringContentProvider("application/json", callback.toString(), StandardCharsets.UTF_8));

        ContentResponse response = execute(request);

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw error(response);
        }

        return response.getContentAsString();
    }

    public void deleteCallback(String callbackId) throws TedeeApiException {
        Request request = client.newRequest(base + "/callback/" + callbackId).method(HttpMethod.DELETE);
        ContentResponse response = execute(request);

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw error(response);
        }
    }

    private void post(String p) throws TedeeApiException {
        Request q = client.newRequest(base + p).method(HttpMethod.POST);
        q.header("Content-Length", "0");
        ContentResponse r = execute(q);
        if (r.getStatus() != 204) {
            throw error(r);
        }
    }

    private ContentResponse execute(Request q) throws TedeeApiException {
        q.header("api_token", encryptedToken());
        q.header("Accept", "application/json");
        try {
            return q.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TedeeApiException("Interrupted", 408, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TedeeApiException("HTTP failed: " + e.getMessage(), 500, e);
        }
    }

    private String encryptedToken() throws TedeeApiException {
        long ts = System.currentTimeMillis();
        try {
            String h = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest((token + ts).getBytes(StandardCharsets.UTF_8)));
            return h + ts;
        } catch (Exception e) {
            throw new TedeeApiException("SHA-256 unavailable", 500, e);
        }
    }

    private TedeeApiException error(ContentResponse r) {
        return new TedeeApiException("Tedee HTTP " + r.getStatus() + " " + r.getReason(), r.getStatus());
    }
}
